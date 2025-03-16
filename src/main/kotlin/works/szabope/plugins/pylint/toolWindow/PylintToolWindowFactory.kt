package works.szabope.plugins.pylint.toolWindow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowType
import com.intellij.ui.content.ContentFactory
import org.jetbrains.annotations.VisibleForTesting
import works.szabope.plugins.common.toolWindow.TreeManager
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.services.PylintSeverityConfigService

@VisibleForTesting
internal open class PylintToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val severities = PylintSeverityConfigService.getInstance(project).getAll().map { it.level }.toSet()
        val panel = PylintToolWindowPanel(project, createTreeManager(severities))
        val content =
            ContentFactory.getInstance().createContent(panel, PylintBundle.message("pylint.toolwindow.name"), false)
        toolWindow.contentManager.addContent(content)
        toolWindow.setType(ToolWindowType.DOCKED, null)
    }

    @VisibleForTesting
    protected open fun createTreeManager(severities: Set<String>) = TreeManager(severities = severities)
}
