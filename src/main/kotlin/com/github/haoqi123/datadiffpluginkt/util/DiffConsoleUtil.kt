package com.github.haoqi123.datadiffpluginkt.util

import com.intellij.database.editor.DatabaseEditorHelper
import com.intellij.database.model.DasNamespace
import com.intellij.database.psi.DbElement
import com.intellij.database.util.DasUtil
import com.intellij.database.vfs.DatabaseElementVirtualFileImpl

object DiffConsoleUtil {

   fun console(dbElement: DbElement, resultString: String) {
        val file = DatabaseElementVirtualFileImpl.findFile(dbElement, false)!!
        file.isBusy = false
        file.setBinaryContent(resultString.encodeToByteArray())

        DatabaseEditorHelper.openConsoleForFile(
            dbElement.project,
            dbElement.dataSource,
            DasUtil.getParentOfClass(dbElement, DasNamespace::class.java, false), file
        )
    }
}