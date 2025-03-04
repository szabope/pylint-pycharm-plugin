package works.szabope.plugins.pylint

import com.intellij.configurationStore.deserializeState
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.JDOMUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.waitUntil
import com.jetbrains.python.target.PyTargetAwareAdditionalData
import io.mockk.*
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.pylint.dialog.IDialogManager
import works.szabope.plugins.pylint.dialog.PylintExecutionErrorDialog
import works.szabope.plugins.pylint.services.OldPylintSettings
import works.szabope.plugins.pylint.services.PylintPackageManagementFacade
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

@Suppress("UnstableApiUsage")
@TestDataPath("\$CONTENT_ROOT/testData/configuration")
class PylintConfigurationTest : AbstractToolWindowTestCase() {

    private lateinit var dialogManager: TestDialogManager
    private val mockSdkPath = "${Paths.get(testDataPath).absolutePathString()}/MockSdk"

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
        val oldStateXml = JDOMUtil.load(
            """<component name="PylintConfigService">
                   <option name="customPylintPath" value="$mockSdkPath/bin/pylint" />
                   <option name="pylintArguments" value="--some-arg 8" />
                   <option name="pylintrcPath" value="${testDataPath}/configuration.toml" />
                   <option name="scanBeforeCheckin" value="true" />
               </component>""".trimIndent()
        )
        val oldState = deserializeState(oldStateXml, OldPylintSettings.OldPylintSettingsState::class.java)
        val oldSettings = OldPylintSettings.getInstance(project)
        val settings = PylintSettings.getInstance(project)
        settings.reset()
        oldSettings.loadState(oldState!!)
        try {
            runBlocking { triggerReconfiguration() }
            with(settings) {
                TestCase.assertFalse(useProjectSdk)
                assertEquals(oldSettings.executablePath, executablePath)
                assertEquals(oldSettings.configFilePath, configFilePath)
                assertEquals(oldSettings.arguments, arguments)
                assertEquals(oldSettings.isScanBeforeCheckIn, isScanBeforeCheckIn)
            }
        } finally {
            oldSettings.reset()
        }
    }

    fun testProjectSdkSelectedWhenSet() = withMockSdk(mockSdkPath) {
        runBlocking { PylintPackageManagementFacade(project).install() }
        val settings = PylintSettings.getInstance(project)
        settings.reset()
        runBlocking { triggerReconfiguration() }
        with(settings) {
            assertTrue(useProjectSdk)
            assertNull(executablePath)
        }
    }

    fun testProjectSdkNotSelectedWhenWsl() = withMockSdk(mockSdkPath) { packageManager ->
        runBlocking { PylintPackageManagementFacade(project).install() }
        // let's lie that it's WSL
        val mockSdk = packageManager.sdk
        val mockAdditionalData = mockk<PyTargetAwareAdditionalData>()
        every { mockAdditionalData.sdkId } returns "WSL ya know what I'm sayin"
        mockkObject(mockSdk)
        every { mockSdk.sdkAdditionalData } returns mockAdditionalData
        val settings = PylintSettings.getInstance(project)
        settings.reset()
        runBlocking { triggerReconfiguration() }
        with(settings) {
            assertFalse(useProjectSdk)
            assertNull(executablePath)
        }
    }

    fun testSdkNotSetIfPylintNotInstalled() = withMockSdk(mockSdkPath) {
        val settings = PylintSettings.getInstance(project)
        settings.reset()
        runBlocking { triggerReconfiguration() }
        with(settings) {
            assertFalse(useProjectSdk)
            assertNull(executablePath)
        }
    }

    fun testStartedWithIncorrectConfigResultsInConfigCleanup() = withMockSdk(mockSdkPath) {
        val settings = PylintSettings.getInstance(project)
        settings.reset()
        settings.useProjectSdk = true
        runBlocking { triggerReconfiguration() }
        with(settings) {
            assertFalse(useProjectSdk)
            assertNull(executablePath)
        }
    }

    fun testExistingCliPathTakesPrecedenceOverProjectSdk() = withMockSdk(mockSdkPath) {
        val settings = PylintSettings.getInstance(project)
        settings.reset()
        settings.executablePath = "$mockSdkPath/bin/pylint"
        runBlocking { triggerReconfiguration() }
        assertFalse(settings.useProjectSdk)
        assertEquals("$mockSdkPath/bin/pylint", settings.executablePath)
    }

    fun testObsoleteVersionIsNotSet() {
        PylintSettings.getInstance(project).executablePath = null
        val pathToObsoletePylint = Paths.get(testDataPath).resolve("pylint_obsolete").absolutePathString()
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
