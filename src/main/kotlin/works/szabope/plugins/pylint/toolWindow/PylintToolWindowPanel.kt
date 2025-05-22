package works.szabope.plugins.pylint.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.treeStructure.Tree
import org.jetbrains.annotations.VisibleForTesting
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.common.toolWindow.AbstractToolWindowPanel

class PylintToolWindowPanel(project: Project, @VisibleForTesting val tree: Tree = Tree()) :
    AbstractToolWindowPanel(project, tree) {

    init {
        super.init(ID, MAIN_ACTION_GROUP, object : AutoScrollConfig {
            override var isAutoScrollToSource
                get() = Settings.getInstance(project).isAutoScrollToSource
                set(value) {
                    Settings.getInstance(project).isAutoScrollToSource = value
                }
            override val tree
                get() = this@PylintToolWindowPanel.tree
            override val placeholderActionId = SCROLL_TO_SOURCE_ID
        })
    }

    companion object {
        private const val MAIN_ACTION_GROUP: String = "works.szabope.plugins.pylint.PylintPluginActions"
        const val ID = "Pylint "
        const val SCROLL_TO_SOURCE_ID = "works.szabope.plugins.pylint.action.ScrollToSourceAction"

        @JvmStatic
        fun getInstance(project: Project) =
            ToolWindowManager.getInstance(project).getToolWindow(ID)!!.contentManager.contents.single().component
    }
}