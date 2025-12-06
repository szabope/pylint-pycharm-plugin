package works.szabope.plugins.pylint.toolWindow

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.toolWindow.AbstractTreeService
import works.szabope.plugins.common.toolWindow.ITreeService
import works.szabope.plugins.pylint.services.pylintSeverityConfigs

@Service(Service.Level.PROJECT)
class PylintTreeService : AbstractTreeService(pylintSeverityConfigs.keys) {
    companion object {
        @JvmStatic
        fun getInstance(project: Project): ITreeService = project.service<PylintTreeService>()
    }
}