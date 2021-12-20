package com.github.haoqi123.datadiffpluginkt.util

import com.intellij.database.psi.DbNamespaceImpl
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.psi.PsiElement
import java.util.stream.Stream

object PluginExistsUtils {
    @Volatile
    private var existsDatabaseTools: Boolean? = null

    fun existsDbTools(e: AnActionEvent) {
        var visible: Boolean? = null
        val psiElements = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY)
        if (psiElements == null || psiElements.size == 0) {
            visible = false
        }
        val existsDbTools = existsDbTools()
        if (!existsDbTools) {
            visible = false
        }
        if (visible == null) {
            if (!Stream.of(*psiElements).allMatch { item: PsiElement? ->
                    CheckMatch.checkAssignableFrom(
                        item!!.javaClass
                    )
                }) {
                visible = false
            }
        }
        if (visible != null) {
            e.presentation.isEnabledAndVisible = visible
        }
    }

    private fun existsDbTools(): Boolean {
        if (existsDatabaseTools == null) {
            synchronized(PluginExistsUtils::class.java) {
                if (existsDatabaseTools == null) {
                    try {
                        Class.forName("com.intellij.database.psi.DbTable")
                        existsDatabaseTools = true
                    } catch (ex: ClassNotFoundException) {
                        existsDatabaseTools = false
                    }
                }
            }
        }
        return existsDatabaseTools!!
    }

    private object CheckMatch {
        fun checkAssignableFrom(aClass: Class<out PsiElement?>?): Boolean {
            return try {
                DbNamespaceImpl::class.java.isAssignableFrom(aClass)
            } catch (e: Exception) {
                false
            }
        }
    }
}