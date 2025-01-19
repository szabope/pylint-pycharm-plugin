package works.szabope.plugins.pylint.toolWindow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowType
import com.intellij.ui.content.ContentFactory
import org.jetbrains.annotations.VisibleForTesting
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.messages.IScanResultListener
import works.szabope.plugins.pylint.messages.ScanResultPublisher

@VisibleForTesting
internal open class PylintToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = createPanel(project)
        project.messageBus.connect(toolWindow.disposable)
            .subscribe(ScanResultPublisher.SCAN_RESULT_TOPIC, IScanResultListener {
                panel.addScanResult(it)
            })
        val content =
            ContentFactory.getInstance().createContent(panel, PylintBundle.message("pylint.toolwindow.name"), false)
        toolWindow.contentManager.addContent(content)
        toolWindow.setType(ToolWindowType.DOCKED, null)
    }

    @VisibleForTesting
    protected open fun createPanel(project: Project): PylintToolWindowPanel {
        return PylintToolWindowPanel(project)
    }
}
