package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import works.szabope.plugins.pylint.services.PylintSeverityConfigService

class SeverityFiltersActionGroup : DumbAware, ActionGroup() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    //TODO: cache
    override fun getChildren(e: AnActionEvent?) =
        PylintSeverityConfigService.getInstance(requireNotNull(e?.project)).getAll().map { SeverityFilterAction(it) }
            .toTypedArray()

    companion object {
        const val ID = "works.szabope.plugins.pylint.ErrorLevelDisplayOptions.SeverityFilters"
    }
}