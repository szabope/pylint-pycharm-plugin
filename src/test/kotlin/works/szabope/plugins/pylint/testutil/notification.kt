package works.szabope.plugins.pylint.testutil

import com.intellij.notification.ActionCenter
import com.intellij.notification.Notification
import com.intellij.openapi.project.Project
import works.szabope.plugins.pylint.PylintBundle

fun getConfigurationNotCompleteNotification(project: Project): Notification {
    return ActionCenter.getNotifications(project).single {
        PylintBundle.message("notification.group.pylint.group") == it.groupId && PylintBundle.message("pylint.notification.incomplete_configuration") == it.content && !it.isExpired
    }
}