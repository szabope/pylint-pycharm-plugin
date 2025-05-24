package works.szabope.plugins.pylint.toolWindow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowType
import com.intellij.ui.content.ContentFactory
import works.szabope.plugins.pylint.PylintBundle

class PylintToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = PylintToolWindowPanel(project)
        val content =
            ContentFactory.getInstance().createContent(panel, PylintBundle.message("pylint.toolwindow.name"), false)
        toolWindow.contentManager.addContent(content)
        toolWindow.setType(ToolWindowType.DOCKED, null)
    }
}
