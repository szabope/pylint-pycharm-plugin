package works.szabope.plugins.pylint.action

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.action.AbstractInstallToolAction
import works.szabope.plugins.common.action.InstallationToolActionConfig
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.services.PylintPackageManagementFacade

class InstallPylintAction : AbstractInstallToolAction(
    InstallationToolActionConfig(
        PylintBundle.message("action.InstallPylintAction.in_progress"),
        PylintBundle.message("action.InstallPylintAction.done_html"),
        PylintBundle.message("action.InstallPylintAction.fail_html")
    )
) {
    override fun getPackageManager(project: Project) = PylintPackageManagementFacade(project)

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.InstallPylintAction"
    }
}
