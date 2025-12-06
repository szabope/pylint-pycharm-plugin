package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VirtualFile
import works.szabope.plugins.pylint.toolWindow.PylintTreeService

class RescanAction : ScanAction() {

    override fun listTargets(event: AnActionEvent): Collection<VirtualFile> {
        return PylintTreeService.getInstance(event.project ?: return emptyList()).getRootScanPaths()
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.RescanAction"
    }
}