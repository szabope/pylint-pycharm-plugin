package works.szabope.plugins.pylint.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.services.IncompleteConfigurationNotifier
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.action.InstallPylintAction
import works.szabope.plugins.pylint.action.OpenSettingsAction

@Service(Service.Level.PROJECT)
class PylintIncompleteConfigurationNotifier(project: Project) : IncompleteConfigurationNotifier(
    project,
    PylintBundle.message("notification.group.pylint.group"),
    PylintBundle.message("pylint.notification.incomplete_configuration"),
    OpenSettingsAction.ID,
    InstallPylintAction.ID,
) {
    companion object {
        @JvmStatic
        fun getInstance(project: Project): PylintIncompleteConfigurationNotifier =
            project.service<PylintIncompleteConfigurationNotifier>()
    }
}
