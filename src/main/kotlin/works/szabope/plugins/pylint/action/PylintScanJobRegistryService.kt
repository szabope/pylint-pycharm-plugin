package works.szabope.plugins.pylint.action

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.action.AbstractScanJobRegistry

@Service(Service.Level.PROJECT)
class PylintScanJobRegistryService : AbstractScanJobRegistry() {
    companion object {
        fun getInstance(project: Project): PylintScanJobRegistryService = project.service()
    }
}