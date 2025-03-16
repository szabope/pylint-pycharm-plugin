package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.waitUntil
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.pylint.AbstractToolWindowTestCase
import works.szabope.plugins.pylint.services.AsyncScanService
import works.szabope.plugins.pylint.testutil.scan
import works.szabope.plugins.pylint.testutil.stopScan
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath("\$CONTENT_ROOT/testData/action/stop_scan")
class StopScanTest : AbstractToolWindowTestCase() {

    override fun getTestDataPath() = "src/test/testData/action/stop_scan"

    override fun setUp() {
        super.setUp()
        with(Settings.getInstance(project)) {
            reset()
            executablePath = Paths.get(testDataPath).resolve("pylint").absolutePathString()
            projectDirectory = Paths.get(testDataPath).absolutePathString()
        }
    }

    /**
     * For infinite loop check `pylint` shell script on test's data path
     */
    fun `test that we can stop an external process that runs an infinite loop`() {
        val file = myFixture.configureByText("a.py", "doesn't matter").virtualFile
        scan(getContext { it.add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(file)) })
        stopScan(getContext())
        runBlocking { waitUntil { !AsyncScanService.getInstance(project).scanInProgress } }
    }
}