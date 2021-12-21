package com.github.haoqi123.datadiffpluginkt.eventlog

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project

fun notifyError(project: Project, content: String) {
/*    NotificationGroupManager.getInstance()
        .getNotificationGroup("Custom Notification Group")
        .createNotification(content, NotificationType.ERROR)
        .notify(project)*/

    /*  if (!ClassLoader.getPlatformClassLoader().isNullOr("com.intellij.notification.Notifications")) {
        val notification = Class.forName("com.intellij.notification.Notification")
             .getConstructor(
                 Class.forName("java.lang.String"),
                 Class.forName("java.lang.String"),
                 Class.forName("java.lang.String"),
                 Class.forName("com.intellij.notification.NotificationType")
             )
             .newInstance(
                 "Custom Notification Group", "", content,
                 Class.forName("com.intellij.notification.NotificationType").enumConstants[2]
             )
         Class.forName("com.intellij.notification.Notifications\$Bus").getMethod(
             "notify",
             Class.forName("com.intellij.notification.Notification"),
             Class.forName("com.intellij.openapi.project.Project")
         ).invoke(null, notification, project)
         return
     }*/

    Notifications.Bus.notify(
        Notification("Custom Notification Group", "", content, NotificationType.ERROR),
        project
    )
}
//
///**
// * Shows the notification[Notification].
// */
//fun Notification.show(project: Project? = null) {
//    Notifications.Bus.notify(this, project)
//}