@file:Suppress("removal")

package works.szabope.plugins.common.dialog

import com.intellij.openapi.components.service
import com.intellij.webcore.packaging.PackageManagementService
import com.jetbrains.python.packaging.ui.PyPackageManagementService.PyPackageInstallationErrorDescription
import org.jetbrains.annotations.Nls

interface PluginDialog {
    fun show()
    fun getExitCode(): Int = 0
}

interface IDialogManager {
    fun showDialog(dialog: PluginDialog)

    fun createPyPackageInstallationErrorDialog(
        @Nls title: String, errorDescription: PyPackageInstallationErrorDescription
    ): PluginDialog

    fun createPackagingErrorDialog(
        @Nls title: String, errorDescription: PackageManagementService.ErrorDescription
    ): PluginDialog

    fun createToolExecutionErrorDialog(command: String, result: String, resultCode: Int): PluginDialog

    fun createToolOutputParseErrorDialog(command: String, commandOutput: String, error: String): PluginDialog

    companion object {
        fun showPyPackageInstallationErrorDialog(
            @Nls title: String, errorDescription: PyPackageInstallationErrorDescription
        ) = with(dialogManager()) {
            val dialog = createPyPackageInstallationErrorDialog(title, errorDescription)
            showDialog(dialog)
        }

        fun showPackagingErrorDialog(
            @Nls title: String, errorDescription: PackageManagementService.ErrorDescription
        ) = with(dialogManager()) {
            val dialog = createPackagingErrorDialog(title, errorDescription)
            showDialog(dialog)
        }

        fun showToolExecutionErrorDialog(command: String, result: String, resultCode: Int) = with(dialogManager()) {
            val dialog = createToolExecutionErrorDialog(command, result, resultCode)
            showDialog(dialog)
        }

        fun showToolOutputParseErrorDialog(command: String, commandOutput: String, error: String) = with(dialogManager()) {
            val dialog = createToolOutputParseErrorDialog(command, commandOutput, error)
            showDialog(dialog)
        }

        private fun dialogManager(): IDialogManager {
            return service<IDialogManager>()
        }
    }
}