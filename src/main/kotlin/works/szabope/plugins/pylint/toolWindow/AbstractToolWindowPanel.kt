package works.szabope.plugins.pylint.toolWindow

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataSink
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.ui.AutoScrollToSourceHandler
import com.intellij.ui.TreeUIHelper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EditSourceOnDoubleClickHandler
import com.intellij.util.EditSourceOnEnterKeyHandler
import com.intellij.util.ui.JBUI
import works.szabope.plugins.common.toolWindow.IssueNodeUserObject
import works.szabope.plugins.common.toolWindow.TreeManager
import works.szabope.plugins.pylint.action.ScrollToSourceDummyAction
import works.szabope.plugins.pylint.services.PylintSettings
import java.awt.BorderLayout
import javax.swing.Box
import kotlin.io.path.Path

abstract class AbstractToolWindowPanel(private val project: Project, private val treeManager: TreeManager) :
    SimpleToolWindowPanel(false, true) {

    fun init(toolWindowId: String, mainActionGroupId: String) {
        treeManager.modelManager.addChangeListener {
            repaint()
            ActivityTracker.getInstance().inc()
        }
        border = JBUI.Borders.empty(1)
        addAutoScrollToSource(AutoScrollConfig(project, treeManager.tree))
        addToolbar(toolWindowId, mainActionGroupId)
        addPane(treeManager.tree)
    }

    override fun uiDataSnapshot(sink: DataSink) {
        treeManager.uiDataSnapshot(sink)
        sink.lazy(CommonDataKeys.NAVIGATABLE) {
            val userObject = treeManager.getSelectedNodeUserObject() as? IssueNodeUserObject? ?: return@lazy null
            val file = VfsUtil.findFile(Path(userObject.file), true) ?: return@lazy null
            OpenFileDescriptor(project, file, userObject.line, userObject.column)
        }
        super.uiDataSnapshot(sink)
    }

    private fun addPane(tree: Tree) {
        add(JBScrollPane(tree), BorderLayout.CENTER)
        EditSourceOnDoubleClickHandler.install(tree)
        EditSourceOnEnterKeyHandler.install(tree)
        TreeUIHelper.getInstance().installTreeSpeedSearch(tree)
        TreeUIHelper.getInstance().installSmartExpander(tree)
    }

    protected class AutoScrollConfig(private val project: Project, val tree: Tree) {
        var isAutoScrollToSource
            get() = PylintSettings.getInstance(project).isAutoScrollToSource
            set(value) {
                PylintSettings.getInstance(project).isAutoScrollToSource = value
            }

    }

    private fun addAutoScrollToSource(config: AutoScrollConfig) {
        val autoScrollToSourceHandler = object : AutoScrollToSourceHandler() {
            override fun isAutoScrollMode() = config.isAutoScrollToSource

            override fun setAutoScrollMode(state: Boolean) {
                config.isAutoScrollToSource = state
            }
        }
        autoScrollToSourceHandler.install(config.tree)
        ActionManager.getInstance()
            .replaceAction(ScrollToSourceDummyAction.ID, autoScrollToSourceHandler.createToggleAction())
    }

    private fun addToolbar(toolWindowId: String, mainActionGroupId: String) {
        val mainActionGroup = ActionManager.getInstance().getAction(mainActionGroupId) as ActionGroup
        val mainToolbar = ActionManager.getInstance().createActionToolbar(
            toolWindowId, mainActionGroup, false
        )
        mainToolbar.targetComponent = this
        val toolBarBox = Box.createHorizontalBox()
        toolBarBox.add(mainToolbar.component)
        add(toolBarBox, BorderLayout.WEST)
    }
}
