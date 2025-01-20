package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel

class ExpandAllAction : DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        event.getData(PylintToolWindowPanel.PYLINT_PANEL_DATA_KEY)?.expandAll()
    }
}
