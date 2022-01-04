package com.github.haoqi123.services

import com.intellij.openapi.project.Project
import com.github.haoqi123.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
