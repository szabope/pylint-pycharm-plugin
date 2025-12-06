package works.szabope.plugins.pylint.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.action.InstallPylintAction
import works.szabope.plugins.pylint.action.OpenSettingsAction

class IncompleteConfigurationNotifier {
    companion object {
        @JvmStatic
        fun notify(project: Project, canInstall: Boolean) {
            val openSettingsAction = ActionManager.getInstance().getAction(OpenSettingsAction.ID)
            val notificationGroup = NotificationGroupManager.getInstance()
                .getNotificationGroup(PylintBundle.message("notification.group.pylint.group"))
            val notification = notificationGroup.createNotification(
                PylintBundle.message("pylint.notification.incomplete_configuration"), NotificationType.WARNING
            ).addAction(openSettingsAction)
            if (canInstall) {
                val installAction = ActionManager.getInstance().getAction(InstallPylintAction.ID)
                notification.addAction(installAction)
            }
            notification.notify(project)
        }
    }
}
