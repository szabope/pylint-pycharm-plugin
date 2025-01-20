package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareToggleAction
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import works.szabope.plugins.pylint.toolWindow.SeverityConfig

class SeverityFilterAction(private val config: SeverityConfig) :
    DumbAwareToggleAction(config.text, config.description, config.icon) {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun isSelected(event: AnActionEvent): Boolean {
        return event.getData(PylintToolWindowPanel.PYLINT_PANEL_DATA_KEY)?.isSeverityLevelDisplayed(config.level)
            ?: true
    }

    override fun setSelected(event: AnActionEvent, selected: Boolean) {
        event.getData(PylintToolWindowPanel.PYLINT_PANEL_DATA_KEY)?.setSeverityLevelDisplayed(config.level, selected)
    }

    fun getSeverity() = config.level
}