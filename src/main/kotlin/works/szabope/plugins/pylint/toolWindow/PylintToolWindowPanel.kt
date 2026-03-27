package works.szabope.plugins.pylint.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.Tree
import org.jetbrains.annotations.VisibleForTesting
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.common.toolWindow.AbstractToolWindowPanel
import works.szabope.plugins.common.toolWindow.ITreeService
import works.szabope.plugins.pylint.services.PylintSettings

class PylintToolWindowPanel(project: Project, @VisibleForTesting val tree: Tree = Tree()) :
    AbstractToolWindowPanel(project, tree) {

    override val treeService: ITreeService = PylintTreeService.getInstance(project)
    override val settings: Settings = PylintSettings.getInstance(project)

    init {
        super.init(ID, MAIN_ACTION_GROUP, SCROLL_TO_SOURCE_ID)
    }

    companion object {
        private const val MAIN_ACTION_GROUP = "works.szabope.plugins.pylint.PylintPluginActions"
        const val ID = "Pylint "
        const val SCROLL_TO_SOURCE_ID = "works.szabope.plugins.pylint.action.ScrollToSourceAction"
    }
}