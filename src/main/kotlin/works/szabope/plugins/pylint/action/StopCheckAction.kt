package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import works.szabope.plugins.pylint.services.PylintService

class StopCheckAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        PylintService.getInstance(event.project ?: return).cancelScan()
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = PylintService.getInstance(event.project ?: return).scanInProgress
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}