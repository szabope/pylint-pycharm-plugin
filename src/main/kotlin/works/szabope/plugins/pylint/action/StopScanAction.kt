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
            ScanJobRegistry.INSTANCE.cancel()
            event.project?.let { PylintTreeService.getInstance(it) }?.lock()
        }.get()
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = ScanJobRegistry.INSTANCE.isActive()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.StopScanAction"
    }
}