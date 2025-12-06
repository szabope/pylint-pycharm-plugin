// inspired by idea/243.19420.21 git4idea.test.TestDialogManager
package works.szabope.plugins.pylint.testutil

import works.szabope.plugins.common.dialog.PluginDialog
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.PluginPackageManagementException
import works.szabope.plugins.common.test.dialog.AbstractTestDialogManager
import works.szabope.plugins.common.test.dialog.TestDialogWrapper
import works.szabope.plugins.pylint.dialog.*

class TestDialogManager : AbstractTestDialogManager() {
    override fun createPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException) =
        TestDialogWrapper(
            PylintPackageInstallationErrorDialog::class.java, exception
        )

    override fun createToolExecutionErrorDialog(configuration: ImmutableSettingsData, result: String, resultCode: Int) =
        TestDialogWrapper(PylintExecutionErrorDialog::class.java, configuration, result, resultCode)

    override fun createFailedToExecuteErrorDialog(message: String): PluginDialog =
        TestDialogWrapper(FailedToExecuteErrorDialog::class.java, message)

    override fun createToolOutputParseErrorDialog(
        configuration: ImmutableSettingsData, targets: String, json: String, error: String
    ) = TestDialogWrapper(PylintParseErrorDialog::class.java, configuration, targets, json, error)

    override fun createGeneralErrorDialog(failure: Throwable) =
        TestDialogWrapper(PylintGeneralErrorDialog::class.java, failure)
}
