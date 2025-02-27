package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import works.szabope.plugins.pylint.services.AsyncScanService
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel

abstract class AbstractScanAction : DumbAwareAction() {
    protected fun isReadyToScan(project: Project): Boolean {
        return PylintSettings.getInstance(project).isComplete() && !AsyncScanService.getInstance(project).scanInProgress
    }

    fun getTreeModelManager(event: AnActionEvent) = event.getData(PylintToolWindowPanel.TREE_MODEL_MANAGER)
}
