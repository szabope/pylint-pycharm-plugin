package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.WriteIntentReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.pyi.PyiFileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import works.szabope.plugins.pylint.services.parser.PylintMessageConverter
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.services.ScanService
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import works.szabope.plugins.pylint.toolWindow.PylintTreeService

val SUPPORTED_FILE_TYPES = arrayOf(PythonFileType.INSTANCE, PyiFileType.INSTANCE)

open class ScanAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val targets = listTargets(event) ?: return
        val project = event.project ?: return
        val treeService = PylintTreeService.getInstance(project)
        treeService.reinitialize(targets)
        WriteIntentReadAction.run { FileDocumentManager.getInstance().saveAllDocuments() }
        val job = currentThreadCoroutineScope().launch(Dispatchers.IO) {
            val configuration = PylintSettings.getInstance(project).getValidConfiguration().getOrNull() ?: return@launch
            ScanService.getInstance(project).scanAsync(targets, configuration).forEach {
                val message = PylintMessageConverter.convert(it)
                withContext(Dispatchers.EDT) {
                    treeService.add(message)
                }
            }
            treeService.lock()
        }
        PylintScanJobRegistryService.getInstance(project).set(job)
        ToolWindowManager.getInstance(project).getToolWindow(PylintToolWindowPanel.ID)?.show()
    }

    override fun update(event: AnActionEvent) {
        val targets = listTargets(event) ?: return
        event.presentation.isEnabled = event.project?.let { isReadyToScan(it, targets) } == true
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    protected open fun listTargets(event: AnActionEvent): Collection<VirtualFile>? {
        return event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.asList()
    }

    private fun isReadyToScan(project: Project, targets: Collection<VirtualFile>): Boolean {
        return targets.isNotEmpty() && PylintScanJobRegistryService.getInstance(project).isAvailable() && PylintSettings.getInstance(project)
            .getValidConfiguration().isSuccess && isEligibleTargets(targets)
    }

    private fun isEligibleTargets(targets: Collection<VirtualFile>) = targets.map { isEligible(it) }.all { it }

    private fun isEligible(virtualFile: VirtualFile) =
        virtualFile.fileType in SUPPORTED_FILE_TYPES || virtualFile.isDirectory

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.ScanAction"
    }
}
