package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.ex.temp.TempFileSystem
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.waitUntilAssertSucceeds
import io.mockk.every
import io.mockk.mockkObject
import junit.framework.Assert
import junit.framework.AssertionFailedError
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.pylint.AbstractToolWindowTestCase
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.dialog.DialogManager
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.testutil.*
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@Suppress("removal")
@TestDataPath($$"$CONTENT_ROOT/testData/action/scan_sdk")
class ScanSdkTest : AbstractToolWindowTestCase() {

    private val dialogManager = TestDialogManager()

    override fun getTestDataPath() = "src/test/testData/action/scan_sdk"

    override fun setUp() {
        mockkObject(DialogManager.Companion)
        every { DialogManager.dialogManager } answers { dialogManager }
        super.setUp()
    }

    fun testManualScan() = withMockSdk("${Paths.get(testDataPath).absolutePathString()}/MockSdk") {
        myFixture.copyDirectoryToProject("/", "/")
        installPylint(dataContext(project) { add(CommonDataKeys.PROJECT, project) })
        setUpSettings()
        val excludedDir = TempFileSystem.getInstance().findFileByPath("/src/excluded_dir")!!
        val exclusionContext = dataContext(project) {
            add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(excludedDir))
        }
        markExcluded(exclusionContext)
        var assertionError: Error? = null
        toolWindowManager.onBalloon {
            val expected = PylintBundle.message("action.InstallPylintAction.done_html")
            if (expected != it.htmlBody) {
                assertionError = AssertionFailedError(Assert.format("Should not happen", expected, it.htmlBody))
            }
        }
        val target = TempFileSystem.getInstance().findFileByPath("/src")!!
        val context = dataContext(project) { add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(target)) }
        waitForIt(ScanAction.ID, context)
        scan(context)
        PlatformTestUtil.waitWhileBusy { PylintScanJobRegistryService.getInstance(project).isActive() }
        assertionError?.let { throw it }
        runBlocking {
            waitUntilAssertSucceeds {
                treeUtil.assertStructure("+Found 2 issue(s) in 1 file(s)\n")
            }.also {
                treeUtil.expandAll()
                treeUtil.assertStructure(
                    """|-Found 2 issue(s) in 1 file(s)
                   | -/src/action/scan_cli/manualScan.py
                   |  [disallowed-name] Disallowed name "tata"
                   |  [disallowed-name] Disallowed name "tutu"
                   |""".trimMargin()
                )
            }
        }
        unmark(exclusionContext)
    }

    private fun setUpSettings() {
        with(PylintSettings.getInstance(project)) {
            executablePath = ""
            workingDirectory = Paths.get(testDataPath).absolutePathString()
            useProjectSdk = true
            configFilePath = ""
            scanBeforeCheckIn = false
            arguments = ""
            excludeNonProjectFiles = true
        }
    }
}
