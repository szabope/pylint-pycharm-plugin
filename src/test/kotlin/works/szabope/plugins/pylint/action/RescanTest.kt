package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import works.szabope.plugins.pylint.AbstractToolWindowTestCase
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.testutil.dataContext
import works.szabope.plugins.pylint.testutil.scan
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/action/rescan")
class RescanTest : AbstractToolWindowTestCase() {

    override fun getTestDataPath() = "src/test/testData/action/rescan"

    override fun setUp() {
        super.setUp()
        with(PylintSettings.getInstance(project)) {
            reset()
            executablePath = Paths.get(testDataPath).resolve("pylint").absolutePathString()
            workingDirectory = Paths.get(testDataPath).absolutePathString()
        }
        val file = myFixture.configureByText("a.py", "doesn't matter").virtualFile
        scan(dataContext(project) { add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(file)) })
        PlatformTestUtil.waitWhileBusy { ScanJobRegistry.INSTANCE.isActive() }
    }

    /**
     * To be sure that rescan was actually called, we reconfigure executable to a version that returns pylint results: `pylint2`
     * `pylint` executable returns no results
     */
    fun `test rescan running for the same file scan did`() {
        PylintSettings.getInstance(project).executablePath =
            Paths.get(testDataPath).resolve("pylint2").absolutePathString()
        PlatformTestUtil.invokeNamedAction(RescanAction.ID)
        PlatformTestUtil.waitWhileBusy { ScanJobRegistry.INSTANCE.isActive() }
        treeUtil.assertStructure("+Found 1 issue(s) in 1 file(s)\n")
        treeUtil.expandAll()
        treeUtil.assertStructure(
            """|-Found 1 issue(s) in 1 file(s)
                   | -/src/a.py
                   |  [fake-convention-symbol] Convention issue
                   |""".trimMargin()
        )
    }
}