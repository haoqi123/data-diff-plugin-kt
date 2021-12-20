package com.github.haoqi123.datadiffpluginkt.listeners

import com.github.haoqi123.datadiffpluginkt.action.FILE_1
import com.github.haoqi123.datadiffpluginkt.action.FILE_2
import com.github.haoqi123.datadiffpluginkt.services.MyProjectService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.util.io.FileUtil
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.IOException

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        project.service<MyProjectService>()

        if (!File(FILE_1).exists()) {
            try {
                File(FILE_1).createNewFile()
                FileUtil.writeToFile(File(FILE_1), StringUtils.EMPTY)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        if (!File(FILE_2).exists()) {
            try {
                File(FILE_2).createNewFile()
                FileUtil.writeToFile(File(FILE_2), StringUtils.EMPTY)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
