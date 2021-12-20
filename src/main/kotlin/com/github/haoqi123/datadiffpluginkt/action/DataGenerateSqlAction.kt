package com.github.haoqi123.datadiffpluginkt.action

import com.github.haoqi123.datadiffpluginkt.eventlog.notifyError
import com.github.haoqi123.datadiffpluginkt.ui.ChooseWrapper
import com.github.haoqi123.datadiffpluginkt.util.DataDiffUtil
import com.github.haoqi123.datadiffpluginkt.util.SqlUtil
import com.intellij.database.editor.DatabaseEditorHelper
import com.intellij.database.model.DasNamespace
import com.intellij.database.psi.DbElement
import com.intellij.database.psi.DbNamespaceImpl
import com.intellij.database.util.DasUtil
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

class DataGenerateSqlAction : CompareFilesAction(), DumbAware {

    val FILE_1 = "/file1.sql"
    val FILE_2 = "/file2.sql"

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
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
        val filePatch1 = FileUtil.getTempDirectory() + FILE_1
        val filePatch2 = FileUtil.getTempDirectory() + FILE_2
        val lastSelection_1 = LocalFileSystem.getInstance().findFileByPath(filePatch1)
        val lastSelection_2 = LocalFileSystem.getInstance().findFileByPath(filePatch2)
        try {
            FileUtil.writeToFile(File(filePatch1), sourceTableText)
        } catch (ignored: IOException) {
        }

        try {
            FileUtil.writeToFile(File(filePatch2), targetTableText)
        } catch (ignored: IOException) {
        }

        val dbElement = psi[1] as DbElement

        val file = DatabaseElementVirtualFileImpl.findFile(dbElement, false)
        file!!.isBusy = false
        file.setContent(resultString, hashCode(dbElement))

        DatabaseEditorHelper.openConsoleForFile(
            dbElement.project,
            dbElement.dataSource,
            DasUtil.getParentOfClass(dbElement, DasNamespace::class.java, false), file
        )


        val chain = BaseShowDiffAction.createMutableChainFromFiles(project, lastSelection_1!!, lastSelection_2!!)
        DiffManager.getInstance().showDiff(project, chain, DiffDialogHints.DEFAULT)
    }


    override fun isAvailable(e: AnActionEvent): Boolean {
        return false
    }

    override fun getDiffRequest(e: AnActionEvent): DiffRequest? {
        return e.getData(DIFF_REQUEST)
    }


    private fun hashCode(element: DbElement): Int {
//        val e: BasicElement = DbImplUtilCore.getMaybeBasicElement(element)
//        return if (e == null) 0 else ModelLightCopierUtils.hashCode(e)
        return 0
    }

    override fun update(e: AnActionEvent) {
//        PluginExistsUtils.existsDbTools(e)
    }

    override fun isDumbAware(): Boolean {
        return super.isDumbAware()
    }
}