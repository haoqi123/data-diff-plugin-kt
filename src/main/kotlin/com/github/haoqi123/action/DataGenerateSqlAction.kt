package com.github.haoqi123.action

import com.github.haoqi123.eventlog.notifyError
import com.github.haoqi123.ui.ChooseWrapper
import com.github.haoqi123.util.DataDiffUtil
import com.github.haoqi123.util.DiffConsoleUtil
import com.github.haoqi123.util.PluginExistsUtils
import com.github.haoqi123.util.SqlUtil
import com.intellij.database.psi.DbElement
import com.intellij.database.psi.DbNamespaceImpl
import com.intellij.diff.actions.CompareFilesAction
import com.intellij.diff.requests.DiffRequest
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiElement
import java.io.File

val FILE_1: String = FileUtil.getTempDirectory() + File.separator + "file1_source.sql"
val FILE_2 = FileUtil.getTempDirectory() + File.separator + "file2_target.sql"

class DataGenerateSqlAction : CompareFilesAction(), DumbAware {


    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project!!
        val data = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY)
        if (data == null) {
            notifyError(project, "Did Not Choose Or Choose Wrong")
            return
        }
        if (data.size != 2) {
            notifyError(project, "Must Choose Two")
            return
        }

        if (data[0] !is DbNamespaceImpl || data[1] !is DbNamespaceImpl) {
            notifyError(project, "Go To Select The Schema")
            return
        }

        val chooseWrapper = ChooseWrapper(project)
        chooseWrapper.setData(data)
        chooseWrapper.show()
        if (chooseWrapper.exitCode != Messages.YES) {
            return
        }
        val psi: Array<PsiElement> = chooseWrapper.psi

        //打开对比结果页签
        DiffConsoleUtil.console(
            psi[1] as DbElement, SqlUtil.getResultString(
                DataDiffUtil.getDiffResult(
                    SqlUtil.getData(psi[0] as DbNamespaceImpl),
                    SqlUtil.getData(psi[1] as DbNamespaceImpl)
                ), psi[0] as DbNamespaceImpl
            )
        )

        //打开对比页签
//        consoleDiff(psi, project)
    }

    override fun isAvailable(e: AnActionEvent): Boolean {
        return false
    }

    override fun getDiffRequest(e: AnActionEvent): DiffRequest? {
        return e.getData(DIFF_REQUEST)
    }

    override fun update(e: AnActionEvent) {
        PluginExistsUtils.existsDbTools(e)
    }
//
//    fun consoleDiff(psi: Array<PsiElement>, project: Project) {
//        val sourceTableText = SqlUtil.getTableText(psi[0])
//        val targetTableText = SqlUtil.getTableText(psi[1])
//        val lastSelection1 = LocalFileSystem.getInstance().findFileByPath(FILE_1)!!
//        val lastSelection2 = LocalFileSystem.getInstance().findFileByPath(FILE_2)!!
//        try {
//            FileUtil.writeToFile(File(FILE_1), sourceTableText)
//        } catch (ignored: IOException) {
//        }
//
//        try {
//            FileUtil.writeToFile(File(FILE_2), targetTableText)
//        } catch (ignored: IOException) {
//        }
//
//        val chain = BaseShowDiffAction.createMutableChainFromFiles(project, lastSelection1, lastSelection2)
//        DiffManager.getInstance().showDiff(project, chain, DiffDialogHints.DEFAULT)
//    }
}