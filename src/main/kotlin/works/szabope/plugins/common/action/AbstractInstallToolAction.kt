@file:Suppress("removal")

package works.szabope.plugins.common.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.jetbrains.python.packaging.ui.PyPackageManagementService.PyPackageInstallationErrorDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.common.services.PackageManagementFacade
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.pylint.dialog.IDialogManager
import works.szabope.plugins.pylint.services.OldPylintSettings
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel

class InstallationToolActionConfig(
    val messageInstalling: String,
    val messageInstalled: String,
    val messageInstallationFailed: String,
)

abstract class AbstractInstallToolAction(private val config: InstallationToolActionConfig) : DumbAwareAction() {
    abstract fun getPackageManager(project: Project): PackageManagementFacade

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        runWithModalProgressBlocking(project, config.messageInstalling) {
            withContext(Dispatchers.EDT) {
                val errorDescription = getPackageManager(project).install()
                if (errorDescription == null) {
                    ToolWindowManager.getInstance(project).notifyByBalloon(
                        PylintToolWindowPanel.ID, MessageType.INFO, config.messageInstalled
                    )
                    Settings.getInstance(project).initSettings(OldPylintSettings.getInstance(project))
                } else {
                    val title = config.messageInstallationFailed
                    if (errorDescription is PyPackageInstallationErrorDescription) {
                        IDialogManager.showPyPackageInstallationErrorDialog(title, errorDescription)
                    } else {
                        IDialogManager.showPackagingErrorDialog(title, errorDescription)
                    }
                }
            }
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project?.let { getPackageManager(it).canInstall() } ?: false
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}