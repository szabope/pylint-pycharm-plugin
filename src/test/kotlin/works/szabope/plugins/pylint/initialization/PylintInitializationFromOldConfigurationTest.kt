package works.szabope.plugins.pylint.initialization

import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.PlatformTestUtil
import works.szabope.plugins.pylint.AbstractPylintHeavyPlatformTestCase
import works.szabope.plugins.pylint.services.PylintSettings
import java.nio.file.Path

class PylintInitializationFromOldConfigurationTest : AbstractPylintHeavyPlatformTestCase() {

    override fun setUpProject() {
        VfsRootAccess.allowRootAccess(testRootDisposable, "/usr/bin")
        myProject = PlatformTestUtil.loadAndOpenProject(Path.of(PROJECT_PATH), getTestRootDisposable())
    }

    fun `test plugin initialized from old configuration`() {
        with(PylintSettings.getInstance(project)) {
            PlatformTestUtil.waitWhileBusy { !isInitialized() }
            assertFalse(useProjectSdk)
            assertEquals("$PROJECT_PATH/.venv/bin/pylint", executablePath)
            assertEquals("--ignore-comments=y", arguments)
            assertFalse(scanBeforeCheckIn)
            assertEquals("$PROJECT_PATH/.pylintrc", configFilePath)
        }
    }

    companion object {
        const val PROJECT_PATH = "src/test/testData/initialization/OldConfiguration"
    }
}
