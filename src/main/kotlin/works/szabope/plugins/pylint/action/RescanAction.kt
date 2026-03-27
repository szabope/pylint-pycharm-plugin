package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VirtualFile

class RescanAction : ScanAction() {

    override fun listTargets(event: AnActionEvent): Collection<VirtualFile>? {
        val project = event.project ?: return null
        return getTreeService(project).getRootScanPaths()
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.RescanAction"
    }
}