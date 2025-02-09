// inspired by idea/243.19420.21 git4idea.DialogManager
@file:Suppress("removal")

package works.szabope.plugins.pylint.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.webcore.packaging.PackageManagementService
import com.intellij.webcore.packaging.PackagingErrorDialog
import com.jetbrains.python.packaging.PyPackageInstallationErrorDialog
import com.jetbrains.python.packaging.ui.PyPackageManagementService.PyPackageInstallationErrorDescription
import org.jetbrains.annotations.Nls

private fun DialogWrapper.toPylintDialog() = object : PylintDialog {
    override fun show() = this@toPylintDialog.show()
}

class DialogManager : IDialogManager {
    override fun showDialog(dialog: PylintDialog) = dialog.show()

    override fun createPyPackageInstallationErrorDialog(
        @Nls title: String, errorDescription: PyPackageInstallationErrorDescription
    ) = PyPackageInstallationErrorDialog(title, errorDescription).toPylintDialog()

    override fun createPackagingErrorDialog(
        @Nls title: String, errorDescription: PackageManagementService.ErrorDescription
    ) = PackagingErrorDialog(title, errorDescription).toPylintDialog()

    override fun createPylintExecutionErrorDialog(command: String, result: String, resultCode: Int) =
        PylintExecutionErrorDialog(command, result, resultCode).toPylintDialog()

    override fun createPylintParseErrorDialog(command: String, commandOutput: String, error: String) =
        PylintParseErrorDialog(command, commandOutput, error).toPylintDialog()

    override fun createPreCheckinConfirmationDialog(project: Project, errorCount: Int, commitButtonText: String) =
        PreCheckinConfirmationDialog(project, errorCount, commitButtonText).toPylintDialog()
}
