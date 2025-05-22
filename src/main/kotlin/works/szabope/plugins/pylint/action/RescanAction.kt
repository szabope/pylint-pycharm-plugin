package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAwareAction
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.common.toolWindow.ITreeService
import works.szabope.plugins.pylint.services.AsyncScanService
import works.szabope.plugins.pylint.services.parser.PylintPublishingToolOutputHandler

class RescanAction : DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val treeService = ITreeService.getInstance(project)
        val latestScanTargets = treeService.getRootScanPaths()
        treeService.reinitialize(latestScanTargets)
        FileDocumentManager.getInstance().saveAllDocuments()
        AsyncScanService.getInstance(project).scan(
            latestScanTargets, Settings.getInstance(project).getData(), PylintPublishingToolOutputHandler(project)
        )
    }

    override fun update(event: AnActionEvent) {
        val project = event.project ?: return
        val treeService = ITreeService.getInstance(project)
        event.presentation.isEnabled =
            treeService.getRootScanPaths().isNotEmpty() && ScanActionUtil.isReadyToScan(project)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.RescanAction"
    }
}