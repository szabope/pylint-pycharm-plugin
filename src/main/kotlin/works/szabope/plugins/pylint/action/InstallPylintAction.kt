package works.szabope.plugins.pylint.action

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.common.action.AbstractInstallToolAction
import works.szabope.plugins.common.services.PluginPackageManagementException
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.dialog.DialogManager
import works.szabope.plugins.pylint.services.PylintPluginPackageManagementService
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel

class InstallPylintAction : AbstractInstallToolAction(PylintBundle.message("action.InstallPylintAction.done_html")) {
    override fun getPackageManager(project: Project) = PylintPluginPackageManagementService.getInstance(project)

    override fun handleFailure(failure: Throwable) {
        when (failure) {
            is PluginPackageManagementException.InstallationFailedException -> {
                DialogManager.showPyPackageInstallationErrorDialog(failure)
            }

            else -> {
                thisLogger().error(failure)
                DialogManager.showGeneralErrorDialog(failure)
            }
        }
    }

    override fun notifyPanel(project: Project, message: String) {
        ToolWindowManager.getInstance(project).notifyByBalloon(
            PylintToolWindowPanel.ID, MessageType.INFO, message
        )
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.InstallPylintAction"
    }
}
