package com.github.haoqi123.datadiffpluginkt.eventlog

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

fun notifyError(project: Project, notificationType: NotificationType, content: String) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("Custom Notification Group")
        .createNotification(content, notificationType)
        .notify(project)
}