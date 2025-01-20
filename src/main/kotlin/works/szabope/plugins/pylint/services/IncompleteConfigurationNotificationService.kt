package works.szabope.plugins.pylint.services

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.action.InstallPylintAction
import works.szabope.plugins.pylint.action.OpenSettingsAction
import java.lang.ref.WeakReference

@Service(Service.Level.PROJECT)
class IncompleteConfigurationNotificationService(private val project: Project) {

    private var notification: WeakReference<Notification> = WeakReference(null)

    @Synchronized
    fun notify(canInstall: Boolean) {
        val notification = NotificationGroupManager.getInstance().getNotificationGroup("Pylint Group")
            .createNotification(PylintBundle.message("pylint.settings.incomplete"), NotificationType.WARNING)
        val openSettingsAction = ActionManager.getInstance().getAction(OpenSettingsAction.ID)
        notification.addAction(
            NotificationAction.create(
                PylintBundle.message("pylint.intention.complete_configuration.text")
            ) { event, _ ->
                run {
                    ActionUtil.performActionDumbAwareWithCallbacks(openSettingsAction, event)
                    notification.hideBalloon()
                }
            })
        if (canInstall) {
            val installAction = ActionManager.getInstance().getAction(InstallPylintAction.ID)
            notification.addAction(
                NotificationAction.create(
                    PylintBundle.message("pylint.intention.install_pylint.text"),
                ) { event, _ ->
                    run {
                        ActionUtil.performActionDumbAwareWithCallbacks(installAction, event)
                        notification.expire()
                    }
                })
        }
        this.notification.get()?.expire()
        this.notification = WeakReference(notification)
        notification.notify(project)
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): IncompleteConfigurationNotificationService = project.service()
    }
}