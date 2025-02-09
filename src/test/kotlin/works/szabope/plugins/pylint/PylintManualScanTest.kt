package works.szabope.plugins.pylint

import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.waitUntil
import com.intellij.ui.tree.TreeTestUtil
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.pylint.dialog.IDialogManager
import works.szabope.plugins.pylint.dialog.PylintParseErrorDialog
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.testutil.TestDialogManager
import works.szabope.plugins.pylint.testutil.TestDialogWrapper
import works.szabope.plugins.pylint.testutil.scan
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import java.net.URL
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import javax.swing.event.HyperlinkEvent
import kotlin.io.path.absolutePathString
import kotlin.io.path.pathString

@TestDataPath("\$CONTENT_ROOT/testData/manualScan")
class PylintManualScanTest : AbstractToolWindowTestCase() {

    private val treeUtil = TreeTestUtil(tree)
    private lateinit var dialogManager: TestDialogManager

    override fun getTestDataPath() = "src/test/testData/manualScan"

    override fun setUp() {
        super.setUp()
        dialogManager = service<IDialogManager>() as TestDialogManager
    }

    override fun tearDown() {
        if (::dialogManager.isInitialized) dialogManager.cleanup()
        super.tearDown()
    }

    fun testManualScan() = runBlocking {
        setUpSettings("pylint")
        val testName = getTestName(true)
        val file = myFixture.configureByFile("$testName.py")
        scan(file)
        waitUntil {
            try {
                treeUtil.assertStructure("+Found 2 issue(s) in 1 file(s)\n")
            } catch (ignored: AssertionError) {
                return@waitUntil false
            }
            true
        }.also {
            treeUtil.expandAll()
            treeUtil.assertStructure(
                """|-Found 2 issue(s) in 1 file(s)
                   | -/src/manualScan.py
                   |  [disallowed-name] Disallowed name "tata"
                   |  [disallowed-name] Disallowed name "tutu"
                   |""".trimMargin()
            )
        }
    }

    fun testFailingScan() = runBlocking {
        toolWindowManager.onBalloon(PylintToolWindowPanel.ID) {
            it.listener?.hyperlinkUpdate(
                HyperlinkEvent(
                    "dumb", HyperlinkEvent.EventType.ACTIVATED, URL("http://localhost")
                )
            )
        }
        val dialogShown = CompletableFuture<TestDialogWrapper>()
        dialogManager.onDialog(PylintParseErrorDialog::class.java) {
            dialogShown.complete(it)
            DialogWrapper.OK_EXIT_CODE
        }
        setUpSettings("pylint_failing")
        val file = myFixture.configureByFile("manualScan.py")
        scan(file)
        waitUntil {
            dialogShown.isDone && with(dialogShown.get()) { isShown() && getExitCode() == DialogWrapper.OK_EXIT_CODE }
        }
    }

    private fun setUpSettings(executable: String) {
        with(PylintSettings.getInstance(myFixture.project)) {
            executablePath = Paths.get(myFixture.testDataPath).resolve(executable).absolutePathString()
            projectDirectory = Paths.get(myFixture.testDataPath).pathString
            useProjectSdk = false
            configFilePath = null
            isScanBeforeCheckIn = false
            arguments = null
            isExcludeNonProjectFiles = true
        }
    }
}
