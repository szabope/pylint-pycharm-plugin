@file:Suppress("removal")

package works.szabope.plugins.pylint.dialog

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.webcore.packaging.PackageManagementService
import com.jetbrains.python.packaging.ui.PyPackageManagementService.PyPackageInstallationErrorDescription
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.TestOnly

interface PylintDialog {
    fun show()
    fun getExitCode(): Int = 0

    @TestOnly
    fun getWrappedClass(): Class<out Any>

    @TestOnly
    fun close(exitCode: Int) = Unit

    @TestOnly
    fun isShown(): Boolean? = null
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

    fun createPreCheckinConfirmationDialog(project: Project, errorCount: Int, commitButtonText: String): PylintDialog

    @TestOnly
    fun onDialog(dialogClass: Class<out DialogWrapper>, handler: (PylintDialog) -> Int) = Unit

    @TestOnly
    fun cleanup() = Unit

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

        fun showPreCheckinConfirmationDialog(project: Project, errorCount: Int, commitButtonText: String) =
            with(dialogManager()) {
                val dialog = createPreCheckinConfirmationDialog(project, errorCount, commitButtonText)
                showDialog(dialog)
                dialog
            }

        private fun dialogManager(): IDialogManager {
            return service<IDialogManager>()
        }
    }
}