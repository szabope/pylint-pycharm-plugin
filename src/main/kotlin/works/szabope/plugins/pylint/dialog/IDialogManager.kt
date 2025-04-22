@file:Suppress("removal")

package works.szabope.plugins.pylint.dialog

import com.intellij.openapi.components.service
import com.intellij.webcore.packaging.PackageManagementService
import com.jetbrains.python.packaging.ui.PyPackageManagementService.PyPackageInstallationErrorDescription
import org.jetbrains.annotations.Nls

interface PylintDialog {
    fun show()
    fun getExitCode(): Int = 0
}

interface IDialogManager {
    fun showDialog(dialog: PylintDialog)

    fun createPyPackageInstallationErrorDialog(
        @Nls title: String, errorDescription: PyPackageInstallationErrorDescription
    ): PylintDialog

    fun createPackagingErrorDialog(
        @Nls title: String, errorDescription: PackageManagementService.ErrorDescription
    ): PylintDialog

    fun createPylintExecutionErrorDialog(command: String, result: String, resultCode: Int): PylintDialog

    fun createPylintParseErrorDialog(command: String, commandOutput: String, error: String): PylintDialog

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

        fun showPylintExecutionErrorDialog(command: String, result: String, resultCode: Int) = with(dialogManager()) {
            val dialog = createPylintExecutionErrorDialog(command, result, resultCode)
            showDialog(dialog)
        }

        fun showPylintParseErrorDialog(command: String, commandOutput: String, error: String) = with(dialogManager()) {
            val dialog = createPylintParseErrorDialog(command, commandOutput, error)
            showDialog(dialog)
        }

        private fun dialogManager(): IDialogManager {
            return service<IDialogManager>()
        }
    }
}