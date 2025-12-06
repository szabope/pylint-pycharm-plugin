package works.szabope.plugins.pylint

import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.replaceService
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import com.intellij.ui.tree.TreeTestUtil
import works.szabope.plugins.pylint.services.pylintSeverityConfigs
import works.szabope.plugins.pylint.testutil.TestToolWindowHeadlessManagerImpl
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowFactory
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import works.szabope.plugins.pylint.toolWindow.PylintTreeService

abstract class AbstractToolWindowTestCase : AbstractPylintTestCase() {

    protected lateinit var treeUtil: TreeTestUtil
    protected lateinit var toolWindowManager: TestToolWindowHeadlessManagerImpl

    override fun setUp() {
        super.setUp()
        toolWindowManager = TestToolWindowHeadlessManagerImpl(project)
        project.replaceService(ToolWindowManager::class.java, toolWindowManager, testRootDisposable)
        setUpToolWindow()
        val panel = ToolWindowManager.getInstance(project)
            .getToolWindow(PylintToolWindowPanel.ID)!!.contentManager.contents.single().component as PylintToolWindowPanel
        treeUtil = TreeTestUtil(panel.tree)
        // ensure severities are on default setting
        with(PylintTreeService.getInstance(project)) {
            pylintSeverityConfigs.keys.forEach { assertTrue(isSeverityLevelDisplayed(it)) }
        }
        PlatformTestUtil.waitForAllBackgroundActivityToCalmDown()
    }

    override fun tearDown() {
        toolWindowManager.cleanup()
        super.tearDown()
    }

    private fun setUpToolWindow() {
        val toolWindowManager = ToolWindowManager.getInstance(project) as ToolWindowHeadlessManagerImpl
        val toolWindow = toolWindowManager.doRegisterToolWindow(PylintToolWindowPanel.ID)
        PylintToolWindowFactory().createToolWindowContent(myFixture.project, toolWindow)
    }
}
