package works.szabope.plugins.pylint.configurable

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.validator.AbstractToolValidator
import works.szabope.plugins.common.validator.ToolValidatorMessages
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.services.PylintPluginPackageManagementService

class PylintValidator(project: Project) : AbstractToolValidator(project, MESSAGES) {
    override val versionFlag = "--version"
    override val packageName = "pylint"
    override fun getPackageManagementService() = PylintPluginPackageManagementService.getInstance(project)

    companion object {
        private val MESSAGES = ToolValidatorMessages(
            pathNotExists = PylintBundle.message("pylint.configuration.path_to_executable.not_exists"),
            pathIsDirectory = PylintBundle.message("pylint.configuration.path_to_executable.is_directory"),
            pathNotExecutable = PylintBundle.message("pylint.configuration.path_to_executable.not_executable"),
            unknownVersion = PylintBundle.message("pylint.configuration.path_to_executable.unknown_version"),
            invalidVersion = PylintBundle.message("pylint.configuration.pylint_invalid_version", PylintPluginPackageManagementService.MINIMUM_VERSION),
            notInstalled = PylintBundle.message("pylint.configuration.pylint_not_installed", "Pylint")
        )
    }
}