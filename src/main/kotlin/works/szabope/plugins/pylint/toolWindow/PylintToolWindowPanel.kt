package works.szabope.plugins.pylint.toolWindow

import com.intellij.ide.ActivityTracker
import com.intellij.ide.DefaultTreeExpander
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.AutoScrollToSourceHandler
import com.intellij.ui.TreeUIHelper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EditSourceOnDoubleClickHandler
import com.intellij.util.EditSourceOnEnterKeyHandler
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.tree.TreeUtil
import works.szabope.plugins.pylint.action.ScrollToSourceDummyAction
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.services.parser.PylintMessage
import java.awt.BorderLayout
import javax.swing.Box
import kotlin.io.path.Path

fun getPylintPanel(project: Project): PylintToolWindowPanel? {
    return ToolWindowManager.getInstance(project)
        .getToolWindow(PylintToolWindowPanel.ID)?.contentManager?.getContent(0)?.component as PylintToolWindowPanel?
}

class PylintToolWindowPanel(private val project: Project, private val tree: Tree = Tree()) :
    SimpleToolWindowPanel(false, true) {

    private val displayedSeverityLevels = SeverityConfig.ALL.map { it.level }.toMutableSet()
    private val treeManager = TreeModelManager(displayedSeverityLevels)
    private val treeExpander = DefaultTreeExpander(tree)

    init {
        treeManager.install(tree)
        border = JBUI.Borders.empty(1)
        add(JBScrollPane(tree), BorderLayout.CENTER)
        addToolbar()
        EditSourceOnDoubleClickHandler.install(tree)
        EditSourceOnEnterKeyHandler.install(tree)
        TreeUIHelper.getInstance().installTreeSpeedSearch(tree)
        TreeUIHelper.getInstance().installSmartExpander(tree)
    }

    override fun uiDataSnapshot(sink: DataSink) {
        sink[PYLINT_PANEL_DATA_KEY] = this
        sink.lazy(CommonDataKeys.NAVIGATABLE) {
            val userObject = TreeUtil.getLastUserObject(tree.selectionPath) as? IssueNodeUserObject? ?: return@lazy null
            val file = VfsUtil.findFile(Path(userObject.file), true) ?: return@lazy null
            OpenFileDescriptor(project, file, userObject.line, userObject.column)
        }
        super.uiDataSnapshot(sink)
    }

    fun expandAll() = treeExpander.expandAll()
    fun collapseAll() = treeExpander.collapseAll()

    fun initializeResultTree(targets: Collection<VirtualFile>) {
        treeManager.reinitialize(targets)
    }

    fun addScanResult(scanResult: PylintMessage) {
        val item = with(scanResult) {
            val severity = requireNotNull(SeverityConfig.find(type)) {
                "Pylint message with type '$type' is not supported. Please, report this issue at  https://github.com/szabope/pylint-pycharm-plugin/issues"
            }
            TreeModelDataItem(absolutePath, line, column, message, symbol, severity)
        }
        treeManager.add(item)
        repaint()
        ActivityTracker.getInstance().inc()
    }

    fun isSeverityLevelDisplayed(severityLevel: String): Boolean {
        return displayedSeverityLevels.contains(severityLevel)
    }

    fun setSeverityLevelDisplayed(severityLevel: String, isDisplayed: Boolean) {
        val hadEffect = if (isDisplayed) {
            displayedSeverityLevels.add(severityLevel)
        } else {
            displayedSeverityLevels.remove(severityLevel)
        }
        if (hadEffect) {
            treeManager.reload()
        }
    }

    fun getScanTargets(): Collection<VirtualFile> {
        return treeManager.getRootScanPaths()
    }

    private fun addToolbar() {
        val autoScrollToSourceHandler = object : AutoScrollToSourceHandler() {
            override fun isAutoScrollMode() = PylintSettings.getInstance(project).isAutoScrollToSource

            override fun setAutoScrollMode(state: Boolean) {
                PylintSettings.getInstance(project).isAutoScrollToSource = state
            }
        }
        autoScrollToSourceHandler.install(tree)
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

        @JvmStatic
        val PYLINT_PANEL_DATA_KEY: DataKey<PylintToolWindowPanel> = DataKey.create("PylintToolWindowPanel")

        const val ID = "Pylint "
    }
}