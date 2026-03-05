package works.szabope.plugins.pylint.services

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.services.notifyIncompleteConfiguration
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.action.InstallPylintAction
import works.szabope.plugins.pylint.action.OpenSettingsAction

class IncompleteConfigurationNotifier {
    companion object {
        @JvmStatic
        fun notify(project: Project, canInstall: Boolean) {
            notifyIncompleteConfiguration(
                project,
                PylintBundle.message("notification.group.pylint.group"),
                PylintBundle.message("pylint.notification.incomplete_configuration"),
                OpenSettingsAction.ID,
                InstallPylintAction.ID,
                canInstall
            )
        }
    }
}
