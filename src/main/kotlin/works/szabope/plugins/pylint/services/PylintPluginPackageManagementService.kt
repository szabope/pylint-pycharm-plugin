package works.szabope.plugins.pylint.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.python.packaging.PyRequirement
import com.jetbrains.python.packaging.pyRequirement
import com.jetbrains.python.packaging.requirement.PyRequirementRelation
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService

@Service(Service.Level.PROJECT)
class PylintPluginPackageManagementService(override val project: Project) : AbstractPluginPackageManagementService() {

    override fun getRequirement(): PyRequirement = pyRequirement("pylint", PyRequirementRelation.GTE, MINIMUM_VERSION)

    companion object {
        const val MINIMUM_VERSION = "3.0"

        @JvmStatic
        fun getInstance(project: Project): AbstractPluginPackageManagementService =
            project.service<PylintPluginPackageManagementService>()
    }
}
