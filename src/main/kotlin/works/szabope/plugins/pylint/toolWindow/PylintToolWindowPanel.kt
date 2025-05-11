package works.szabope.plugins.pylint.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.common.toolWindow.AbstractToolWindowPanel

class PylintToolWindowPanel(project: Project) : AbstractToolWindowPanel(project) {

    init {
        super.init(ID, MAIN_ACTION_GROUP)
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