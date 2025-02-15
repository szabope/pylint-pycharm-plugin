package works.szabope.plugins.pylint

import com.intellij.configurationStore.deserializeState
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.JDOMUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.waitUntil
import io.mockk.coEvery
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.pylint.dialog.IDialogManager
import works.szabope.plugins.pylint.dialog.PylintExecutionErrorDialog
import works.szabope.plugins.pylint.services.OldPylintSettings
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.services.cli.Cli
import works.szabope.plugins.pylint.testutil.TestDialogManager
import works.szabope.plugins.pylint.testutil.TestDialogWrapper
import java.io.File
import java.net.URL
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import javax.swing.event.HyperlinkEvent
import kotlin.io.path.absolutePathString

@TestDataPath("\$CONTENT_ROOT/testData/configuration")
class PylintConfigurationTest : AbstractToolWindowTestCase() {

    private lateinit var dialogManager: TestDialogManager

    override fun setUp() {
        super.setUp()
        dialogManager = service<IDialogManager>() as TestDialogManager
    }

    override fun tearDown() {
        if (::dialogManager.isInitialized) dialogManager.cleanup()
        super.tearDown()
    }

    override fun getTestDataPath() = "src/test/testData/configuration"

    @Suppress("UnstableApiUsage")
    fun testInitializeFromOldSettings() {
        myFixture.copyFileToProject("configuration.toml")
        val oldStateXml = JDOMUtil.load(
            """<component name="PylintConfigService">
                   <option name="pylintArguments" value="--some-arg 8" />
                   <option name="pylintrcPath" value="${testDataPath}/configuration.toml" />
               </component>""".trimIndent()
        )
        val oldState = deserializeState(oldStateXml, OldPylintSettings.OldPylintSettingsState::class.java)
        val oldSettings = OldPylintSettings.getInstance(project)
        val settings = PylintSettings.getInstance(myFixture.project)
        settings.reset()
        oldSettings.loadState(oldState!!)
        runBlocking { triggerReconfiguration() }
        with(settings) {
            assertNull(executablePath)
            assertEquals(oldSettings.configFilePath, configFilePath)
            assertEquals(oldSettings.arguments, arguments)
        }
    }

    fun testObsoleteVersionIsNotSet() {
        PylintSettings.getInstance(project).executablePath = null
        val pathToObsoletePylint = Paths.get(myFixture.testDataPath).resolve("pylint_obsolete").absolutePathString()
        PylintSettings.getInstance(project).executablePath = pathToObsoletePylint
        assertNull(PylintSettings.getInstance(project).executablePath)
    }

    fun testConfigFileNotExist() {
        assertFalse(File("THIS-FILE-SHOULD-NOT-EXIST").exists())
        with(PylintSettings.getInstance(project)) {
            {
                configFilePath = null
                configFilePath = "THIS-FILE-SHOULD-NOT-EXIST"
                assertNull(configFilePath)
            }
        }
    }

    fun testConfigFileIsADirectory() {
        myFixture.copyDirectoryToProject("dummy_dir", "/")
        with(PylintSettings.getInstance(project)) {
            {
                configFilePath = null
                configFilePath = "/dummy_dir"
                assertNull(configFilePath)
            }
        }
    }

    fun testProjectDirectoryIsAFile() {
        myFixture.copyFileToProject("dummy")
        with(PylintSettings.getInstance(project)) {
            projectDirectory = null
            projectDirectory = "dummy"
            assertNull(projectDirectory)
        }
    }

    fun testAutodetectFailing() {
        toolWindowManager.onBalloon {
            it.listener?.hyperlinkUpdate(
                HyperlinkEvent(
                    "dumb", HyperlinkEvent.EventType.ACTIVATED, URL("http://localhost")
                )
            )
        }
        val dialogShown = CompletableFuture<TestDialogWrapper>()
        dialogManager.onDialog(PylintExecutionErrorDialog::class.java) {
            dialogShown.complete(it)
            DialogWrapper.OK_EXIT_CODE
        }
        mockkObject(Cli)
        coEvery { Cli.execute("which", "pylint", workDir = any(), env = any()) } returns Cli.Status(2, emptyList(), "")
        runBlocking {
            val result = PylintSettings.getInstance(project).autodetectExecutable()
            assertNull(result)
            waitUntil {
                dialogShown.isDone && with(dialogShown.get()) { isShown() && getExitCode() == DialogWrapper.OK_EXIT_CODE }
            }
        }
        unmockkObject(Cli)
    }
//
//    fun testEnsureValid() {
//        TODO()
//    }
//
//    fun testIsComplete() {
//        TODO()
//    }
}
