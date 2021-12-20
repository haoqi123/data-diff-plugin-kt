package com.github.haoqi123.datadiffpluginkt.listeners

import com.github.haoqi123.datadiffpluginkt.action.FILE_1
import com.github.haoqi123.datadiffpluginkt.action.FILE_2
import com.github.haoqi123.datadiffpluginkt.services.MyProjectService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import java.io.File

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        project.service<MyProjectService>()

        File(FILE_1)
        File(FILE_2)
    }
}
