package works.szabope.plugins.pylint.action

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.common.action.AbstractInstallToolAction
import works.szabope.plugins.common.action.InstallationToolActionConfig
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.services.OldPylintSettings
import works.szabope.plugins.pylint.services.PylintPackageManagementFacade
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel

class InstallPylintAction : AbstractInstallToolAction(
    InstallationToolActionConfig(
        PylintBundle.message("action.InstallPylintAction.in_progress"),
        PylintBundle.message("action.InstallPylintAction.done_html"),
        PylintBundle.message("action.InstallPylintAction.fail_html")
    )
) {
    override fun getPackageManager(project: Project) = PylintPackageManagementFacade(project)

    override suspend fun notifyPanel(project: Project, message: String) {
        ToolWindowManager.getInstance(project).notifyByBalloon(
            PylintToolWindowPanel.ID, MessageType.INFO, message
        )
    }

    override suspend fun migrateSettings(project: Project) {
        Settings.getInstance(project).initSettings(OldPylintSettings.getInstance(project))
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.InstallPylintAction"
    }
}
