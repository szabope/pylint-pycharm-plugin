package works.szabope.plugins.pylint.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.common.toolWindow.AbstractToolWindowPanel
import works.szabope.plugins.common.toolWindow.TreeManager
import works.szabope.plugins.pylint.services.PylintSettings

class PylintToolWindowPanel(project: Project, treeManager: TreeManager) :
    AbstractToolWindowPanel(project, treeManager) {

    init {
        super.init(ID, MAIN_ACTION_GROUP, object : AutoScrollConfig {
            override var isAutoScrollToSource
                get() = PylintSettings.getInstance(project).isAutoScrollToSource
                set(value) {
                    PylintSettings.getInstance(project).isAutoScrollToSource = value
                }
            override val tree
                get() = treeManager.tree
            override val placeholderActionId = SCROLL_TO_SOURCE_ID
        })
    }

    companion object {
        private const val MAIN_ACTION_GROUP: String = "works.szabope.plugins.pylint.PylintPluginActions"
        const val ID = "Pylint "
        const val SCROLL_TO_SOURCE_ID = "works.szabope.plugins.pylint.action.ScrollToSourceAction"

        @JvmStatic
        fun getInstance(project: Project) = requireNotNull(ToolWindowManager.getInstance(project).getToolWindow(ID)) {
            "todo" //TODO
        }.contentManager.contents.single().component
    }
}