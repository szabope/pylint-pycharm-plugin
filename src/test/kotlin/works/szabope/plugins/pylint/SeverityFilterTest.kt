package works.szabope.plugins.pylint

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import works.szabope.plugins.pylint.action.ScanJobRegistry
import works.szabope.plugins.pylint.action.SeverityFiltersActionGroup
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.services.pylintSeverityConfigs
import works.szabope.plugins.pylint.testutil.dataContext
import works.szabope.plugins.pylint.testutil.scan
import works.szabope.plugins.pylint.toolWindow.PylintTreeService
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/severity")
class SeverityFilterTest : AbstractToolWindowTestCase() {

    override fun getTestDataPath() = "src/test/testData/severity"

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

    override fun tearDown() {
        // set severities back to default
        with(PylintTreeService.getInstance(project)) {
            pylintSeverityConfigs.keys.forEach { setSeverityLevelDisplayed(it, true) }
        }
        super.tearDown()
    }

    fun `test all filters selected shows all items`() {
        setSelectedFilters(*pylintSeverityConfigs.keys.toTypedArray())
        treeUtil.assertStructure("+Found 6 issue(s) in 1 file(s)\n")
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

    fun `test no filters selected shows no items`() {
        setSelectedFilters()
        treeUtil.assertStructure("Found 0 issue(s) in 0 file(s)\n")
        treeUtil.expandAll()
        treeUtil.assertStructure("Found 0 issue(s) in 0 file(s)\n")
    }

    fun `test convention selected shows convention-related items only`() {
        setSelectedFilters("convention")
        treeUtil.assertStructure("+Found 1 issue(s) in 1 file(s)\n")
        treeUtil.expandAll()
        treeUtil.assertStructure(
            """|-Found 1 issue(s) in 1 file(s)
           | -/src/a.py
           |  [fake-convention-symbol] Convention issue
           |""".trimMargin()
        )
    }

    @Suppress("UnstableApiUsage")
    private fun setSelectedFilters(vararg activeSeverities: String) {
        val event =
            AnActionEvent.createEvent(
                dataContext(project) {},
                null,
                ActionPlaces.TOOLWINDOW_TOOLBAR_BAR,
                ActionUiKind.NONE,
                null
            )
        val actionGroup = ActionUtil.getActionGroup(SeverityFiltersActionGroup.ID) as SeverityFiltersActionGroup
        actionGroup.getChildren(event).filter { it.isSelected(event) != it.getSeverity() in activeSeverities }.forEach {
            with(AnActionWrapper(it)) {
                update(event)
                actionPerformed(event)
            }
        }
    }
}
