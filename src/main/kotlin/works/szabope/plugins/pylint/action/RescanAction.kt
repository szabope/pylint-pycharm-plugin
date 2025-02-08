package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import works.szabope.plugins.pylint.services.AsyncScanService
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.toRunConfiguration
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel

class RescanAction : AbstractScanAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val panel = event.getData(PylintToolWindowPanel.PYLINT_PANEL_DATA_KEY) ?: return
        val latestScanTargets = panel.getScanTargets()
        val runConfiguration = project.let { PylintSettings.getInstance(it).toRunConfiguration() }
        panel.initializeResultTree(latestScanTargets)
        AsyncScanService.getInstance(project).scan(latestScanTargets, runConfiguration)
    }

    override fun update(event: AnActionEvent) {
        val project = event.project ?: return
        val panel = event.getData(PylintToolWindowPanel.PYLINT_PANEL_DATA_KEY) ?: return
        event.presentation.isEnabled = panel.getScanTargets().isNotEmpty() && isReadyToScan(project)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}