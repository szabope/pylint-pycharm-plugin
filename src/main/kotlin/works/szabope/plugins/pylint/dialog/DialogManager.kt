// inspired by idea/243.19420.21 git4idea.DialogManager
@file:Suppress("removal")

package works.szabope.plugins.pylint.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.webcore.packaging.PackageManagementService
import com.intellij.webcore.packaging.PackagingErrorDialog
import com.jetbrains.python.packaging.PyPackageInstallationErrorDialog
import com.jetbrains.python.packaging.ui.PyPackageManagementService.PyPackageInstallationErrorDescription
import org.jetbrains.annotations.Nls
import works.szabope.plugins.common.dialog.IDialogManager
import works.szabope.plugins.common.dialog.PluginDialog

private fun DialogWrapper.toPylintDialog() = object : PluginDialog {
    override fun show() = this@toPylintDialog.show()
}

class DialogManager : IDialogManager {
    override fun showDialog(dialog: PluginDialog) = dialog.show()

    override fun createPyPackageInstallationErrorDialog(
        @Nls title: String, errorDescription: PyPackageInstallationErrorDescription
    ) = PyPackageInstallationErrorDialog(title, errorDescription).toPylintDialog()

    override fun createPackagingErrorDialog(
        @Nls title: String, errorDescription: PackageManagementService.ErrorDescription
    ) = PackagingErrorDialog(title, errorDescription).toPylintDialog()

    override fun createToolExecutionErrorDialog(command: String, result: String, resultCode: Int) =
        PylintExecutionErrorDialog(command, result, resultCode).toPylintDialog()

    override fun createToolOutputParseErrorDialog(command: String, commandOutput: String, error: String) =
        PylintParseErrorDialog(command, commandOutput, error).toPylintDialog()
}
