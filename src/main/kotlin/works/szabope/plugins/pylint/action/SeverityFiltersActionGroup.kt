package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import works.szabope.plugins.pylint.services.pylintSeverityConfigs

class SeverityFiltersActionGroup : DumbAware, ActionGroup() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    private val children: Array<out SeverityFilterAction> by lazy {
        pylintSeverityConfigs.map { SeverityFilterAction(it.value) }.toTypedArray()
    }

    override fun getChildren(e: AnActionEvent?): Array<out SeverityFilterAction> = children

    companion object {
        const val ID = "works.szabope.plugins.pylint.ErrorLevelDisplayOptions.SeverityFilters"
    }
}