package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.pylint.services.PylintService
import works.szabope.plugins.pylint.services.PylintService.Companion.SUPPORTED_FILE_TYPES
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.toRunConfiguration
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import works.szabope.plugins.pylint.toolWindow.getPylintPanel

open class ScanAction : AbstractScanAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val targets = listTargets(event)?.map { it.path } ?: return
        val project = event.project ?: return
        val runConfiguration = PylintSettings.getInstance(project).toRunConfiguration()
        getPylintPanel(project)?.initializeResultTree(targets)
        PylintService.getInstance(project).scanAsync(targets, runConfiguration)
        ToolWindowManager.getInstance(project).getToolWindow(PylintToolWindowPanel.ID)?.show()
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = isEligibleForScanning(listTargets(event)) && isReadyToScan(
            event.project ?: return
        )
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    protected open fun listTargets(event: AnActionEvent): List<VirtualFile>? {
        return event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.asList()
    }

    private fun isEligibleForScanning(targets: List<VirtualFile>?): Boolean {
        return targets?.isNotEmpty() == true && targets.map { isEligible(it) }.all { it }
    }

    private fun isEligible(virtualFile: VirtualFile): Boolean {
        return virtualFile.fileType in SUPPORTED_FILE_TYPES || virtualFile.isDirectory
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.ScanAction"
    }
}
