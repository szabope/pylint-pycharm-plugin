package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareToggleAction
import org.jetbrains.annotations.VisibleForTesting
import works.szabope.plugins.common.services.SeverityConfig
import works.szabope.plugins.common.toolWindow.TreeManager
import works.szabope.plugins.pylint.PylintBundle

class SeverityFilterAction(private val config: SeverityConfig) :
    DumbAwareToggleAction(config.text, config.description, config.icon) {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun isSelected(event: AnActionEvent): Boolean {
        val severityManager = requireNotNull(event.getData(TreeManager.SEVERITY_MANAGER)) {
            PylintBundle.message("pylint.please_report_this_issue")
        }
        return severityManager.isSeverityLevelDisplayed(config.level)
    }

    override fun setSelected(event: AnActionEvent, selected: Boolean) {
        val severityManager = requireNotNull(event.getData(TreeManager.SEVERITY_MANAGER)) {
            PylintBundle.message("pylint.please_report_this_issue")
        }
        severityManager.setSeverityLevelDisplayed(config.level, selected)
    }

    @VisibleForTesting
    fun getSeverity() = config.level
}