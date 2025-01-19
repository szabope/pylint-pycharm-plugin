package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import works.szabope.plugins.pylint.toolWindow.SeverityConfig

class SeverityFiltersActionGroup : DumbAware, ActionGroup() {

    private val actions = SeverityConfig.ALL.map { SeverityFilterAction(it) }.toTypedArray()

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun getChildren(e: AnActionEvent?) = actions

    companion object {
        const val ID = "works.szabope.plugins.pylint.ErrorLevelDisplayOptions.SeverityFilters"
    }
}