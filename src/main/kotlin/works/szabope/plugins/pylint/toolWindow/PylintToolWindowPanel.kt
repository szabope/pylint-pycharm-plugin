package works.szabope.plugins.pylint.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
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
        })
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