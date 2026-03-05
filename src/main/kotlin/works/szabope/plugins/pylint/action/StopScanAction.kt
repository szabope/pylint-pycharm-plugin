package works.szabope.plugins.pylint.action

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.action.AbstractScanJobRegistry
import works.szabope.plugins.common.action.AbstractStopScanAction
import works.szabope.plugins.common.toolWindow.ITreeService
import works.szabope.plugins.pylint.toolWindow.PylintTreeService

class StopScanAction : AbstractStopScanAction() {

    override fun getScanJobRegistry(project: Project): AbstractScanJobRegistry = PylintScanJobRegistryService.getInstance(project)
    override fun getTreeService(project: Project): ITreeService = PylintTreeService.getInstance(project)

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.StopScanAction"
    }
}
