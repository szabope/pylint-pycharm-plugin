@file:Suppress("removal", "DEPRECATION")

package works.szabope.plugins.common.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.jetbrains.python.packaging.ui.PyPackageManagementService.PyPackageInstallationErrorDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.common.dialog.IDialogManager
import works.szabope.plugins.common.services.PackageManagementFacade

class InstallationToolActionConfig(
    val messageInstalling: String,
    val messageInstalled: String,
    val messageInstallationFailed: String,
)

@Suppress("removal")
abstract class AbstractInstallToolAction(private val config: InstallationToolActionConfig) : DumbAwareAction() {
    abstract fun getPackageManager(project: Project): PackageManagementFacade

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        runWithModalProgressBlocking(project, config.messageInstalling) {
            withContext(Dispatchers.EDT) {
                val errorDescription = getPackageManager(project).installRequirement()
                if (errorDescription == null) {
                    notifyPanel(project, config.messageInstalled)
                    migrateSettings(project)
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

    abstract suspend fun notifyPanel(project: Project, message: String)
    abstract suspend fun migrateSettings(project: Project)
}