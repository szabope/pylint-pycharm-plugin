package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import works.szabope.plugins.pylint.AbstractToolWindowTestCase
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.testutil.dataContext
import works.szabope.plugins.pylint.testutil.scan
import works.szabope.plugins.pylint.testutil.stopScan
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/action/stop_scan")
class StopScanTest : AbstractToolWindowTestCase() {

    override fun getTestDataPath() = "src/test/testData/action/stop_scan"

    override fun setUp() {
        super.setUp()
        with(PylintSettings.getInstance(project)) {
            useProjectSdk = false
            executablePath = Paths.get(testDataPath).resolve("pylint").absolutePathString()
            workingDirectory = Paths.get(testDataPath).absolutePathString()
        }
    }

    /**
     * For infinite loop see `pylint` shell script on test's data path
     */
    fun `test that we can stop an external process that runs an infinite loop`() {
        val file = myFixture.configureByText("a.py", "doesn't matter").virtualFile
        scan(dataContext(project) { add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(file)) })
        PlatformTestUtil.waitWhileBusy { !ScanJobRegistry.INSTANCE.isActive() } // make sure that scan has been started
        stopScan(dataContext(project) {})
        PlatformTestUtil.waitWhileBusy { !ScanJobRegistry.INSTANCE.isAvailable() }
    }
}