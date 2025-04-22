package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import works.szabope.plugins.pylint.services.pylintSeverityConfigs

class SeverityFiltersActionGroup : DumbAware, ActionGroup() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    lateinit var children: Array<out SeverityFilterAction>

    override fun getChildren(e: AnActionEvent?): Array<out SeverityFilterAction> {
        if (!::children.isInitialized) {
            children = pylintSeverityConfigs.map { SeverityFilterAction(it.value) }.toTypedArray()
        }
        return children
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.ErrorLevelDisplayOptions.SeverityFilters"
    }
}