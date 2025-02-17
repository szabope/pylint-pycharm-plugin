package works.szabope.plugins.pylint

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.replaceService
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import com.intellij.ui.treeStructure.Tree
import works.szabope.plugins.pylint.testutil.TestToolWindowHeadlessManagerImpl
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowFactory
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel

abstract class AbstractToolWindowTestCase : AbstractPylintTestCase() {

    protected val tree: Tree = Tree()
    protected lateinit var toolWindowManager: TestToolWindowHeadlessManagerImpl
    protected lateinit var panel: PylintToolWindowPanel

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
        val factory = object : PylintToolWindowFactory() {
            override fun createPanel(project: Project): PylintToolWindowPanel {
                panel = PylintToolWindowPanel(project, tree)
                return panel
            }
        }
        factory.createToolWindowContent(myFixture.project, toolWindow)
    }
}
