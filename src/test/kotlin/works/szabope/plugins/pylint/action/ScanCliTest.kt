package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.ex.temp.TempFileSystem
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import io.mockk.every
import io.mockk.mockkObject
import junit.framework.AssertionFailedError
import org.jetbrains.concurrency.asPromise
import works.szabope.plugins.common.test.dialog.TestDialogWrapper
import works.szabope.plugins.pylint.AbstractToolWindowTestCase
import works.szabope.plugins.pylint.dialog.DialogManager
import works.szabope.plugins.pylint.dialog.PylintExecutionErrorDialog
import works.szabope.plugins.pylint.dialog.PylintParseErrorDialog
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.common.test.action.markExcluded
import works.szabope.plugins.common.test.action.unmark
import works.szabope.plugins.pylint.testutil.*
import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import javax.swing.event.HyperlinkEvent
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/action/scan_cli")
class ScanCliTest : AbstractToolWindowTestCase() {

    private val dialogManager = TestDialogManager()

    override fun getTestDataPath() = "src/test/testData/action/scan_cli"

    override fun setUp() {
        mockkObject(DialogManager.Companion)
        every { DialogManager.dialogManager } answers { dialogManager }
        super.setUp()
    }

    override fun tearDown() {
        dialogManager.cleanup()
        super.tearDown()
    }

    @Suppress("removal")
    fun testManualScan() {
        myFixture.copyDirectoryToProject("/", "/")
        val excludedDir = TempFileSystem.getInstance().findFileByPath("/src/excluded_dir")!!
        setUpSettings("pylint")
        val exclusionContext = dataContext(project) {
            add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(excludedDir))
        }
        markExcluded(exclusionContext)
        var assertionError: Error? = null
        toolWindowManager.onBalloon {
            dialogManager.onAnyDialog { dialog ->
                assertionError = AssertionFailedError(dialog.toString())
            }
            it.listener!!.hyperlinkUpdate(HyperlinkEvent(
                "dumb", HyperlinkEvent.EventType.ACTIVATED, URI("http://localhost").toURL()
            ))
            dialogManager.cleanup()
        }
        val target = TempFileSystem.getInstance().findFileByPath("/src")!!
        scan(dataContext(project) { add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(target)) })
        PlatformTestUtil.waitWhileBusy { PylintScanJobRegistryService.getInstance(project).isActive() }
        assertionError?.let { throw it }

        treeUtil.assertStructure("+Found 2 issue(s) in 1 file(s)\n")
        treeUtil.expandAll()
        treeUtil.assertStructure(
            """|-Found 2 issue(s) in 1 file(s)
                   | -/src/action/scan_cli/manualScan.py
                   |  [disallowed-name] Disallowed name "tata"
                   |  [disallowed-name] Disallowed name "tutu"
                   |""".trimMargin()
        )
        unmark(exclusionContext)
    }

    fun `test invalid output printed to stdout by pylint`() {
        myFixture.copyDirectoryToProject("/", "/")
        setUpSettings("pylint_err_on_stdout")
        val dialogShown = CompletableFuture<TestDialogWrapper>()
        toolWindowManager.onBalloon {
            it.listener?.hyperlinkUpdate(
                HyperlinkEvent(
                    "dumb", HyperlinkEvent.EventType.ACTIVATED, URI("http://localhost").toURL()
                )
            )
        }
        dialogManager.onDialog(PylintParseErrorDialog::class.java) {
            it.close(DialogWrapper.OK_EXIT_CODE)
            dialogShown.complete(it)
            it.getExitCode()
        }
        val target = WorkspaceModel.getInstance(project).currentSnapshot.entities(ContentRootEntity::class.java)
            .first().url.virtualFile!!
        scan(dataContext(project) { add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(target)) })
        PlatformTestUtil.assertPromiseSucceeds(dialogShown.asPromise())
        assertTrue(dialogShown.isDone && with(dialogShown.get()) { isShown() && getExitCode() == DialogWrapper.OK_EXIT_CODE })
    }

    fun `test pylint returning exit code 1`() {
        myFixture.copyDirectoryToProject("/", "/")
        setUpSettings("pylint_exit_1")
        val dialogShown = CompletableFuture<TestDialogWrapper>()
        toolWindowManager.onBalloon {
            it.listener?.hyperlinkUpdate(
                HyperlinkEvent(
                    "dumb", HyperlinkEvent.EventType.ACTIVATED, URI("http://localhost").toURL()
                )
            )
        }
        dialogManager.onDialog(PylintExecutionErrorDialog::class.java) {
            it.close(DialogWrapper.OK_EXIT_CODE)
            dialogShown.complete(it)
            it.getExitCode()
        }
        val target = WorkspaceModel.getInstance(project).currentSnapshot.entities(ContentRootEntity::class.java)
            .first().url.virtualFile!!
        scan(dataContext(project) { add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(target)) })
        PlatformTestUtil.assertPromiseSucceeds(dialogShown.asPromise())
        assertTrue(dialogShown.isDone && with(dialogShown.get()) { isShown() && getExitCode() == DialogWrapper.OK_EXIT_CODE })
    }

    private fun setUpSettings(executable: String) {
        with(PylintSettings.getInstance(project)) {
            executablePath = Paths.get(testDataPath).resolve(executable).absolutePathString()
            workingDirectory = Paths.get(testDataPath).absolutePathString()
            useProjectSdk = false
            configFilePath = ""
            scanBeforeCheckIn = false
            arguments = ""
            excludeNonProjectFiles = true
        }
    }
}
