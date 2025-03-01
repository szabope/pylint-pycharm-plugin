package works.szabope.plugins.pylint.toolWindow

import com.intellij.ide.ActivityTracker
import com.intellij.ide.DefaultTreeExpander
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.AutoScrollToSourceHandler
import com.intellij.ui.TreeUIHelper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EditSourceOnDoubleClickHandler
import com.intellij.util.EditSourceOnEnterKeyHandler
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.tree.TreeUtil
import works.szabope.plugins.common.toolWindow.IssueNodeUserObject
import works.szabope.plugins.common.toolWindow.SeverityManager
import works.szabope.plugins.common.toolWindow.TreeModelManager
import works.szabope.plugins.pylint.action.ScrollToSourceDummyAction
import works.szabope.plugins.pylint.services.PylintSettings
import java.awt.BorderLayout
import javax.swing.Box
import kotlin.io.path.Path

class TreeManager(val tree: Tree = Tree(), severities: Set<String>) : UiDataProvider {
    private val severityManager = SeverityManager(severities)
    val modelManager = TreeModelManager(severityManager::isSeverityLevelDisplayed)
    private val treeExpander = DefaultTreeExpander(tree)

    init {
        severityManager.addChangeListener {
            modelManager.reload()
        }
        modelManager.install(tree)
    }

    override fun uiDataSnapshot(sink: DataSink) {
        sink[PlatformDataKeys.TREE_EXPANDER] = treeExpander
        sink[SEVERITY_MANAGER] = severityManager
        sink[TREE_MODEL_MANAGER] = modelManager
    }

    fun getSelectedNodeUserObject() = TreeUtil.getLastUserObject(tree.selectionPath)

    companion object {
        @JvmStatic
        val SEVERITY_MANAGER: DataKey<SeverityManager> = DataKey.create("PylintToolWindowPanel.severityManager")

        @JvmStatic
        val TREE_MODEL_MANAGER: DataKey<TreeModelManager> = DataKey.create("PylintToolWindowPanel.treeModelManager")
    }
}

class PylintToolWindowPanel(private val project: Project, private val treeManager: TreeManager) :
    SimpleToolWindowPanel(false, true) {

    init {
        treeManager.modelManager.addChangeListener {
            repaint()
            ActivityTracker.getInstance().inc()
        }
        border = JBUI.Borders.empty(1)
        addToolbar()
        val tree = treeManager.tree
        add(JBScrollPane(tree), BorderLayout.CENTER)
        EditSourceOnDoubleClickHandler.install(tree)
        EditSourceOnEnterKeyHandler.install(tree)
        TreeUIHelper.getInstance().installTreeSpeedSearch(tree)
        TreeUIHelper.getInstance().installSmartExpander(tree)
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

    private fun addToolbar() {
        val autoScrollToSourceHandler = object : AutoScrollToSourceHandler() {
            override fun isAutoScrollMode() = PylintSettings.getInstance(project).isAutoScrollToSource

            override fun setAutoScrollMode(state: Boolean) {
                PylintSettings.getInstance(project).isAutoScrollToSource = state
            }
        }
        autoScrollToSourceHandler.install(treeManager.tree)
        val actionManager = ActionManager.getInstance()
        actionManager.replaceAction(ScrollToSourceDummyAction.ID, autoScrollToSourceHandler.createToggleAction())
        val mainActionGroup = actionManager.getAction(MAIN_ACTION_GROUP) as ActionGroup

        val mainToolbar = ActionManager.getInstance().createActionToolbar(
            ID, mainActionGroup, false
        )
        mainToolbar.targetComponent = this
        val toolBarBox = Box.createHorizontalBox()
        toolBarBox.add(mainToolbar.component)
        add(toolBarBox, BorderLayout.WEST)
    }

    companion object {
        private const val MAIN_ACTION_GROUP: String = "works.szabope.plugins.pylint.PylintPluginActions"
        const val ID = "Pylint "

        @JvmStatic
        fun getInstance(project: Project) = requireNotNull(ToolWindowManager.getInstance(project).getToolWindow(ID)) {
            "todo" //TODO
        }.contentManager.contents.single().component
    }
}