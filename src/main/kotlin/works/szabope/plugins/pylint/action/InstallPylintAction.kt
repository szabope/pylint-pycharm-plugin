@file:Suppress("removal")

package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.jetbrains.python.packaging.ui.PyPackageManagementService.PyPackageInstallationErrorDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.dialog.IDialogManager
import works.szabope.plugins.pylint.services.OldPylintSettings
import works.szabope.plugins.pylint.services.PylintPackageManagementFacade
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel

class InstallPylintAction : DumbAwareAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        runWithModalProgressBlocking(project, PylintBundle.message("action.InstallPylintAction.in_progress")) {
            withContext(Dispatchers.EDT) {
                val errorDescription = PylintPackageManagementFacade.install(project)
                if (errorDescription == null) {
                    @Suppress("DialogTitleCapitalization") ToolWindowManager.getInstance(project).notifyByBalloon(
                        PylintToolWindowPanel.ID,
                        MessageType.INFO,
                        PylintBundle.message("action.InstallPylintAction.done_html")
                    )
                    PylintSettings.getInstance(project).initSettings(OldPylintSettings.getInstance(project))
                } else {
                    val title = PylintBundle.message("action.InstallPylintAction.fail_html")
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
        event.presentation.isEnabled = event.project?.let { PylintPackageManagementFacade.canInstall(it) } ?: false
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.InstallPylintAction"
    }
}
