// inspired by idea/243.19420.21 git4idea.test.TestDialogManager
@file:Suppress("removal")

package works.szabope.plugins.pylint.testutil

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.testFramework.requireIs
import com.intellij.webcore.packaging.PackageManagementService
import com.intellij.webcore.packaging.PackagingErrorDialog
import com.jetbrains.python.packaging.PyPackageInstallationErrorDialog
import com.jetbrains.python.packaging.ui.PyPackageManagementService
import org.junit.Assert.assertNull
import works.szabope.plugins.pylint.dialog.*

class TestDialogManager : IDialogManager {
    private val myHandlers = hashMapOf<Class<out DialogWrapper>, (TestDialogWrapper) -> Int>()
    private var myAnyHandler: ((TestDialogWrapper) -> Int)? = null

    override fun showDialog(dialog: PylintDialog) {
        val testDialog = dialog.requireIs<TestDialogWrapper>()
        testDialog.show()
        var exitCode: Int? = null
        try {
            exitCode = myHandlers[testDialog.getWrappedClass()]?.invoke(testDialog) ?: myAnyHandler?.invoke(testDialog)
            if (exitCode == null) {
                throw IllegalStateException("The dialog is not expected here: " + dialog.javaClass)
            }
        } finally {
            testDialog.close(exitCode ?: DialogWrapper.OK_EXIT_CODE)
        }
    }

    override fun createPyPackageInstallationErrorDialog(
        title: String,
        errorDescription: PyPackageManagementService.PyPackageInstallationErrorDescription
    ) = TestDialogWrapper(PyPackageInstallationErrorDialog::class.java)

    override fun createPackagingErrorDialog(
        title: String,
        errorDescription: PackageManagementService.ErrorDescription
    ) = TestDialogWrapper(PackagingErrorDialog::class.java)

    override fun createPylintExecutionErrorDialog(command: String, result: String, resultCode: Int) =
        TestDialogWrapper(PylintExecutionErrorDialog::class.java)

    override fun createPylintParseErrorDialog(command: String, commandOutput: String, error: String) =
        TestDialogWrapper(PylintParseErrorDialog::class.java)

    override fun createPreCheckinConfirmationDialog(
        project: Project,
        errorCount: Int,
        commitButtonText: String
    ) = TestDialogWrapper(PreCheckinConfirmationDialog::class.java)

    fun onDialog(dialogClass: Class<out DialogWrapper>, handler: (TestDialogWrapper) -> Int) {
        assertNull(myHandlers.put(dialogClass, handler))
    }

    fun onAnyDialog(handler: (TestDialogWrapper) -> Int) {
        myAnyHandler = handler
    }

    fun cleanup() {
        myHandlers.clear()
        myAnyHandler = null
    }
}
