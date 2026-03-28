// inspired by idea/243.19420.21 git4idea.test.TestDialogManager
package works.szabope.plugins.pylint.testutil

import works.szabope.plugins.common.services.ToolExecutorConfiguration
import works.szabope.plugins.common.services.PluginPackageManagementException
import works.szabope.plugins.common.test.dialog.AbstractTestDialogManager
import works.szabope.plugins.common.test.dialog.TestDialogWrapper
import works.szabope.plugins.pylint.dialog.*

class TestDialogManager : AbstractTestDialogManager() {
    override fun createPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException) =
        TestDialogWrapper(PylintPackageInstallationErrorDialog::class.java, exception)

    override fun createToolExecutionErrorDialog(configuration: ToolExecutorConfiguration, result: String, resultCode: Int?) =
        TestDialogWrapper(PylintExecutionErrorDialog::class.java, configuration, result, resultCode)

    override fun createToolOutputParseErrorDialog(
        configuration: ToolExecutorConfiguration, targets: String, json: String, error: String
    ) = TestDialogWrapper(PylintParseErrorDialog::class.java, configuration, targets, json, error)

    override fun createGeneralErrorDialog(failure: Throwable) =
        TestDialogWrapper(PylintGeneralErrorDialog::class.java, failure)
}
