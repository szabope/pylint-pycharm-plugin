package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.DumbAwareAction
import kotlinx.coroutines.future.future
import works.szabope.plugins.pylint.toolWindow.PylintTreeService

class StopScanAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        currentThreadCoroutineScope().future {
            event.project?.let { PylintScanJobRegistryService.getInstance(it).cancel() }
            event.project?.let { PylintTreeService.getInstance(it) }?.lock()
        }.get()
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project?.let { PylintScanJobRegistryService.getInstance(it).isActive() } ?: false
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.StopScanAction"
    }
}