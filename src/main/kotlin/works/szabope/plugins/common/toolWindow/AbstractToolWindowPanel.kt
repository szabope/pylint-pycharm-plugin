package works.szabope.plugins.common.toolWindow

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.actionSystem.*
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
import java.awt.BorderLayout
import javax.swing.Box
import kotlin.io.path.Path

abstract class AbstractToolWindowPanel(private val project: Project) : SimpleToolWindowPanel(false, true) {

    protected val treeManager: TreeManager get() = TreeManager.getInstance(project)

    fun init(toolWindowId: String, mainActionGroupId: String, autoScrollConfig: AutoScrollConfig) {
        treeManager.addChangeListener {
            repaint()
            ActivityTracker.getInstance().inc()
        }
        border = JBUI.Borders.empty(1)
        addAutoScrollToSource(autoScrollConfig)
        addToolbar(toolWindowId, mainActionGroupId)
        addPane(treeManager.tree)
    }

    override fun uiDataSnapshot(sink: DataSink) {
        sink[PlatformDataKeys.TREE_EXPANDER] = treeManager.treeExpander
        sink.lazy(CommonDataKeys.NAVIGATABLE) {
            val userObject = treeManager.getSelectedNodeUserObject() ?: return@lazy null
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

    interface AutoScrollConfig {
        var isAutoScrollToSource: Boolean
        val tree: Tree
        val placeholderActionId: String
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
            .replaceAction(config.placeholderActionId, autoScrollToSourceHandler.createToggleAction())
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
