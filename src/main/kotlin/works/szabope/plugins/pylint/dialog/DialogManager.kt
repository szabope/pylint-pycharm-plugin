// inspired by idea/243.19420.21 git4idea.DialogManager
package works.szabope.plugins.pylint.dialog

import com.intellij.openapi.ui.DialogWrapper
import works.szabope.plugins.common.dialog.IDialogManager
import works.szabope.plugins.common.dialog.IDialogManager.IShowDialog
import works.szabope.plugins.common.dialog.PluginDialog
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.PluginPackageManagementException

private fun DialogWrapper.toPylintDialog() = object : PluginDialog {
    override fun show() = this@toPylintDialog.show()
}

class DialogManager : IDialogManager {
    override fun showDialog(dialog: PluginDialog) = dialog.show()

    override fun createPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException) =
        PylintPackageInstallationErrorDialog(exception.message).toPylintDialog()

    override fun createToolExecutionErrorDialog(
        configuration: ImmutableSettingsData,
        result: String,
        resultCode: Int
    ) = PylintExecutionErrorDialog(configuration, result, resultCode).toPylintDialog()

    override fun createFailedToExecuteErrorDialog(message: String) =
        FailedToExecuteErrorDialog(message).toPylintDialog()

    override fun createToolOutputParseErrorDialog(
        configuration: ImmutableSettingsData,
        targets: String,
        json: String,
        error: String
    ) = PylintParseErrorDialog(configuration, targets, json, error).toPylintDialog()

    override fun createGeneralErrorDialog(failure: Throwable) = PylintGeneralErrorDialog(failure).toPylintDialog()

    companion object : IShowDialog {
        override val dialogManager: IDialogManager by lazy { DialogManager() }
    }
}
