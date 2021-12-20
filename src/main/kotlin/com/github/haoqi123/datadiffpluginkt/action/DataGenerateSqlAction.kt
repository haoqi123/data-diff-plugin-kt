package com.github.haoqi123.datadiffpluginkt.action

import com.github.haoqi123.datadiffpluginkt.eventlog.notifyError
import com.github.haoqi123.datadiffpluginkt.ui.ChooseWrapper
import com.github.haoqi123.datadiffpluginkt.util.DataDiffUtil
import com.github.haoqi123.datadiffpluginkt.util.PluginExistsUtils
import com.github.haoqi123.datadiffpluginkt.util.SqlUtil
import com.intellij.database.editor.DatabaseEditorHelper
import com.intellij.database.model.DasNamespace
import com.intellij.database.psi.DbElement
import com.intellij.database.psi.DbNamespaceImpl
import com.intellij.database.script.generator.concatWithSpace
import com.intellij.database.util.DasUtil
import com.intellij.database.util.DbImplUtil
import com.intellij.database.vfs.DatabaseElementVirtualFileImpl
import com.intellij.diff.DiffDialogHints
import com.intellij.diff.DiffManager
import com.intellij.diff.actions.BaseShowDiffAction
import com.intellij.diff.actions.CompareFilesAction
import com.intellij.diff.requests.DiffRequest
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import java.io.File
import java.io.IOException

val FILE_1: String = FileUtil.getTempDirectory().concatWithSpace(File.separator + "file1_source.sql")!!
val FILE_2 = FileUtil.getTempDirectory().concatWithSpace(File.separator + "file2_target.sql")!!

class DataGenerateSqlAction : CompareFilesAction(), DumbAware {


    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project!!
        val data = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY)
        if (data == null) {
            notifyError(project, NotificationType.ERROR, "Did Not Choose Or Choose Wrong")
            return
        }
        if (data.size != 2) {
            notifyError(project, NotificationType.ERROR, "Must Choose Two")
            return
        }

        if (data[0] !is DbNamespaceImpl || data[1] !is DbNamespaceImpl) {
            notifyError(project, NotificationType.ERROR, "Go To Select The Schema")
            return
        }

        val chooseWrapper = ChooseWrapper(project)
        chooseWrapper.setData(data)
        chooseWrapper.show()
        if (chooseWrapper.getExitCode() != Messages.YES) {
            return
        }
        val psi: Array<PsiElement> = chooseWrapper.getPsi()
        val source = psi[0] as DbNamespaceImpl
        val target = psi[1] as DbNamespaceImpl
        val sourceData = SqlUtil.getData(source)
        val targetData = SqlUtil.getData(target)
        val diffResult = DataDiffUtil.getDiffResult(sourceData, targetData)
        val resultString = SqlUtil.getResultString(diffResult, source)


        val sourceTableText = SqlUtil.getTableText(psi[0])
        val targetTableText = SqlUtil.getTableText(psi[1])
        val lastSelection1 = LocalFileSystem.getInstance().findFileByPath(FILE_1)!!
        val lastSelection2 = LocalFileSystem.getInstance().findFileByPath(FILE_2)!!
        try {
            FileUtil.writeToFile(File(FILE_1), sourceTableText)
        } catch (ignored: IOException) {
        }

        try {
            FileUtil.writeToFile(File(FILE_2), targetTableText)
        } catch (ignored: IOException) {
        }

        val dbElement = psi[1] as DbElement

        val file = DatabaseElementVirtualFileImpl.findFile(dbElement, false)!!
        file.isBusy = false
        file.content = resultString

        DatabaseEditorHelper.openConsoleForFile(
            dbElement.project,
            dbElement.dataSource,
            DasUtil.getParentOfClass(dbElement, DasNamespace::class.java, false), file
        )

        val chain = BaseShowDiffAction.createMutableChainFromFiles(project, lastSelection1, lastSelection2)
        DiffManager.getInstance().showDiff(project, chain, DiffDialogHints.DEFAULT)
    }

    override fun isAvailable(e: AnActionEvent): Boolean {
        return false
    }

    override fun getDiffRequest(e: AnActionEvent): DiffRequest? {
        return e.getData(DIFF_REQUEST)
    }

    private fun hashCode(element: DbElement): Int {
        return DbImplUtil.getMaybeBasicElement(element).hashCode()
    }

    override fun update(e: AnActionEvent) {
        PluginExistsUtils.existsDbTools(e)
    }
}