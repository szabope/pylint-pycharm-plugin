package works.szabope.plugins.pylint.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowType
import com.intellij.ui.content.ContentFactory

abstract class MyToolWindowFactory(private val displayName: String) : ToolWindowFactory {
    abstract fun createPanel(project: Project): SimpleToolWindowPanel

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = createPanel(project)
        val content =
            ContentFactory.getInstance().createContent(panel, displayName, false)
        toolWindow.contentManager.addContent(content)
        toolWindow.setType(ToolWindowType.DOCKED, null)
    }
}