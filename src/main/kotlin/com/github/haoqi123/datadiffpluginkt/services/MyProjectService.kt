package com.github.haoqi123.datadiffpluginkt.services

import com.intellij.openapi.project.Project
import com.github.haoqi123.datadiffpluginkt.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
