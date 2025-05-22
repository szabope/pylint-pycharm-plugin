package works.szabope.plugins.pylint

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.waitUntilAssertSucceeds
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.common.toolWindow.ITreeService
import works.szabope.plugins.pylint.action.SeverityFiltersActionGroup
import works.szabope.plugins.pylint.services.pylintSeverityConfigs
import works.szabope.plugins.pylint.testutil.scan
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath("\$CONTENT_ROOT/testData/severity")
class SeverityFilterTest : AbstractToolWindowTestCase() {

    override fun getTestDataPath() = "src/test/testData/severity"

    override fun setUp() {
        super.setUp()
        with(Settings.getInstance(project)) {
            reset()
            executablePath = Paths.get(testDataPath).resolve("pylint").absolutePathString()
            projectDirectory = Paths.get(testDataPath).absolutePathString()
        }
        val file = myFixture.configureByText("a.py", "doesn't matter").virtualFile
        scan(getContext { it.add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(file)) })
    }

    override fun tearDown() {
        // set severities back to default
        with(ITreeService.getInstance(project)) {
            pylintSeverityConfigs.keys.forEach { setSeverityLevelDisplayed(it, true) }
        }
        super.tearDown()
    }

    fun `test all filters selected shows all items`() {
        setSelectedFilters(*pylintSeverityConfigs.keys.toTypedArray())
        runBlocking {
            waitUntilAssertSucceeds { treeUtil.assertStructure("+Found 6 issue(s) in 1 file(s)\n") }.also {
                treeUtil.expandAll()
                treeUtil.assertStructure(
                    """|-Found 6 issue(s) in 1 file(s)
                   | -/src/a.py
                   |  [fake-fatal-symbol] Fatal issue
                   |  [fake-error-symbol] Error issue
                   |  [fake-warning-symbol] Warning issue
                   |  [fake-refactor-symbol] Refactor issue
                   |  [fake-convention-symbol] Convention issue
                   |  [fake-info-symbol] Info issue
                   |""".trimMargin()
                )
            }
        }
    }

    fun `test no filters selected shows no items`() {
        setSelectedFilters()
        runBlocking {
            waitUntilAssertSucceeds { treeUtil.assertStructure("Found 0 issue(s) in 0 file(s)\n") }.also {
                treeUtil.expandAll()
                treeUtil.assertStructure("Found 0 issue(s) in 0 file(s)\n")
            }
        }
    }

    fun `test convention selected shows convention-related items only`() {
        setSelectedFilters("convention")
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

    @Suppress("UnstableApiUsage")
    private fun setSelectedFilters(vararg activeSeverities: String) {
        val event =
            AnActionEvent.createEvent(getContext(), null, ActionPlaces.TOOLWINDOW_TOOLBAR_BAR, ActionUiKind.NONE, null)
        val actionGroup = ActionUtil.getActionGroup(SeverityFiltersActionGroup.ID) as SeverityFiltersActionGroup
        actionGroup.getChildren(event).filter { it.isSelected(event) != it.getSeverity() in activeSeverities }.forEach {
            with(AnActionWrapper(it)) {
                update(event)
                actionPerformed(event)
            }
        }
    }
}
