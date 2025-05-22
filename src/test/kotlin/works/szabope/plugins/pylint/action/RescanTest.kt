package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.waitUntil
import com.intellij.testFramework.common.waitUntilAssertSucceeds
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.pylint.AbstractToolWindowTestCase
import works.szabope.plugins.pylint.services.AsyncScanService
import works.szabope.plugins.pylint.testutil.rescan
import works.szabope.plugins.pylint.testutil.scan
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath("\$CONTENT_ROOT/testData/action/rescan")
class RescanTest : AbstractToolWindowTestCase() {

    override fun getTestDataPath() = "src/test/testData/action/rescan"

    override fun setUp() {
        super.setUp()
        with(Settings.getInstance(project)) {
            reset()
            executablePath = Paths.get(testDataPath).resolve("pylint").absolutePathString()
            projectDirectory = Paths.get(testDataPath).absolutePathString()
        }
        val file = myFixture.configureByText("a.py", "doesn't matter").virtualFile
        scan(getContext { it.add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(file)) })
        runBlocking { waitUntil { !AsyncScanService.getInstance(project).scanInProgress } }
    }

    /**
     * To be sure that rescan was actually called, we reconfigure executable to a version that returns pylint results: `pylint2`
     * `pylint` executable returns no results
     */
    fun `test rescan running for the same file scan did`() {
        Settings.getInstance(project).executablePath =
            Paths.get(testDataPath).resolve("pylint2").absolutePathString()
        rescan(getContext())
        runBlocking {
            waitUntilAssertSucceeds { treeUtil.assertStructure("+Found 1 issue(s) in 1 file(s)\n") }.also {
                treeUtil.expandAll()
                treeUtil.assertStructure(
                    """|-Found 1 issue(s) in 1 file(s)
                   | -/src/a.py
                   |  [fake-convention-symbol] Convention issue
                   |""".trimMargin()
                )
            }
        }
    }
}