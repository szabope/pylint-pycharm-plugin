package works.szabope.plugins.pylint

import com.intellij.ide.ui.IdeUiService
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.replaceService
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import com.intellij.ui.treeStructure.Tree
import works.szabope.plugins.pylint.services.PylintSeverityConfigService
import works.szabope.plugins.pylint.testutil.TestToolWindowHeadlessManagerImpl
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowFactory
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import works.szabope.plugins.common.toolWindow.TreeManager

abstract class AbstractToolWindowTestCase : AbstractPylintTestCase() {

    protected val tree: Tree = Tree()
    protected lateinit var toolWindowManager: TestToolWindowHeadlessManagerImpl
    private lateinit var testContext: DataContext

    override fun setUp() {
        super.setUp()
        toolWindowManager = TestToolWindowHeadlessManagerImpl(project)
        project.replaceService(ToolWindowManager::class.java, toolWindowManager, testRootDisposable)
        setUpToolWindow()
    }

    override fun tearDown() {
        toolWindowManager.cleanup()
        super.tearDown()
    }

    private fun setUpToolWindow() {
        val toolWindowManager = ToolWindowManager.getInstance(project) as ToolWindowHeadlessManagerImpl
        val toolWindow = toolWindowManager.doRegisterToolWindow(PylintToolWindowPanel.ID)
        val factory = object : PylintToolWindowFactory() { //TODO: remove?
            override fun createPanel(project: Project): PylintToolWindowPanel {
                val severities = PylintSeverityConfigService.getInstance(project).getAll().map { it.level }.toSet()
                val panel = PylintToolWindowPanel(project, TreeManager(tree, severities))
                val panelContext = IdeUiService.getInstance().createUiDataContext(panel)
                testContext =
                    SimpleDataContext.builder().setParent(panelContext).add(CommonDataKeys.PROJECT, project).build()
                return panel
            }
        }
        factory.createToolWindowContent(myFixture.project, toolWindow)
    }

    protected fun getContext(customizer: ((SimpleDataContext.Builder) -> SimpleDataContext.Builder)? = null): DataContext {
        if (!::testContext.isInitialized) error("Testing context is not initialized")
        val builder = SimpleDataContext.builder().setParent(testContext)
        customizer?.invoke(builder)
        return builder.build()
    }
}
