// inspired by idea/243.19420.21 git4idea.test.TestDialogManager
@file:Suppress("removal", "DEPRECATION")

package works.szabope.plugins.pylint.testutil

import com.intellij.webcore.packaging.PackageManagementService
import com.intellij.webcore.packaging.PackagingErrorDialog
import com.jetbrains.python.packaging.PyPackageInstallationErrorDialog
import com.jetbrains.python.packaging.ui.PyPackageManagementService
import works.szabope.plugins.common.test.dialog.AbstractTestDialogManager
import works.szabope.plugins.common.test.dialog.TestDialogWrapper
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.pylint.dialog.PylintExecutionErrorDialog
import works.szabope.plugins.pylint.dialog.PylintParseErrorDialog

class TestDialogManager : AbstractTestDialogManager() {
    override fun createPyPackageInstallationErrorDialog(
        title: String, errorDescription: PyPackageManagementService.PyPackageInstallationErrorDescription
    ) = TestDialogWrapper(PyPackageInstallationErrorDialog::class.java, title, errorDescription)

    override fun createPackagingErrorDialog(
        title: String, errorDescription: PackageManagementService.ErrorDescription
    ) = TestDialogWrapper(PackagingErrorDialog::class.java, title, errorDescription)

    override fun createToolExecutionErrorDialog(command: String, result: String, resultCode: Int) =
        TestDialogWrapper(PylintExecutionErrorDialog::class.java, command, result, resultCode)

    override fun createToolOutputParseErrorDialog(
        configuration: ImmutableSettingsData, targets: String, json: String, error: String
    ) = TestDialogWrapper(PylintParseErrorDialog::class.java, configuration, targets, json, error)
}
