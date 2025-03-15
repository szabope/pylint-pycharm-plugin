package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAwareAction
import works.szabope.plugins.pylint.services.AsyncScanService
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.toRunConfiguration

class RescanAction : DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val treeManager = ScanActionUtil.getTreeModelManager(event) ?: return
        val latestScanTargets = treeManager.getRootScanPaths()
        treeManager.reinitialize(latestScanTargets)
        val runConfiguration = project.let { PylintSettings.getInstance(it).toRunConfiguration() }
        FileDocumentManager.getInstance().saveAllDocuments()
        AsyncScanService.getInstance(project).scan(latestScanTargets, runConfiguration)
    }

    override fun update(event: AnActionEvent) {
        val project = event.project ?: return
        val treeManager = ScanActionUtil.getTreeModelManager(event) ?: return
        event.presentation.isEnabled =
            treeManager.getRootScanPaths().isNotEmpty() && ScanActionUtil.isReadyToScan(project)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.RescanAction"
    }
}