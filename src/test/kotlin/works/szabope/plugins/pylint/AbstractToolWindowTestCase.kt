package works.szabope.plugins.pylint

import com.intellij.ide.ui.IdeUiService
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.replaceService
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import com.intellij.ui.tree.TreeTestUtil
import works.szabope.plugins.common.toolWindow.ITreeService
import works.szabope.plugins.pylint.services.pylintSeverityConfigs
import works.szabope.plugins.pylint.testutil.TestToolWindowHeadlessManagerImpl
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowFactory
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel

abstract class AbstractToolWindowTestCase : AbstractPylintTestCase() {

    protected lateinit var treeUtil: TreeTestUtil
    protected lateinit var toolWindowManager: TestToolWindowHeadlessManagerImpl
    private lateinit var testContext: DataContext

    override fun setUp() {
        super.setUp()
        toolWindowManager = TestToolWindowHeadlessManagerImpl(project)
        project.replaceService(ToolWindowManager::class.java, toolWindowManager, testRootDisposable)
        setUpToolWindow()
        val panel = ToolWindowManager.getInstance(project)
            .getToolWindow(PylintToolWindowPanel.ID)!!.contentManager.contents.single().component as PylintToolWindowPanel
        treeUtil = TreeTestUtil(panel.tree)
        val panelContext = IdeUiService.getInstance().createUiDataContext(panel)
        testContext = SimpleDataContext.builder().setParent(panelContext).add(CommonDataKeys.PROJECT, project).build()
        // ensure severities are on default setting
        with(ITreeService.getInstance(project)) {
            pylintSeverityConfigs.keys.forEach { assertTrue(isSeverityLevelDisplayed(it)) }
        }
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

    protected fun getContext(customizer: ((SimpleDataContext.Builder) -> SimpleDataContext.Builder)? = null): DataContext {
        if (!::testContext.isInitialized) error("Testing context is not initialized")
        val builder = SimpleDataContext.builder().setParent(testContext)
        customizer?.invoke(builder)
        return builder.build()
    }

    protected fun getProjectContext(customizer: ((SimpleDataContext.Builder) -> SimpleDataContext.Builder)? = null): DataContext {
        if (!::testContext.isInitialized) error("Testing context is not initialized")
        val builder = SimpleDataContext.builder().setParent(testContext)
        builder.add(CommonDataKeys.PROJECT, project)
        customizer?.invoke(builder)
        return builder.build()
    }
}
