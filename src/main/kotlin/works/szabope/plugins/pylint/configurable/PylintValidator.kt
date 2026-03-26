package works.szabope.plugins.pylint.configurable

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.validator.AbstractToolValidator
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.services.PylintPluginPackageManagementService

class PylintValidator(project: Project) : AbstractToolValidator(project) {
    override val versionFlag = "--version"
    override val packageName = "pylint"
    override fun getPackageManagementService() = PylintPluginPackageManagementService.getInstance(project)
    override fun pathNotExistsMessage() = PylintBundle.message("pylint.configuration.path_to_executable.not_exists")
    override fun pathIsDirectoryMessage() = PylintBundle.message("pylint.configuration.path_to_executable.is_directory")
    override fun pathNotExecutableMessage() = PylintBundle.message("pylint.configuration.path_to_executable.not_executable")
    override fun unknownVersionMessage() = PylintBundle.message("pylint.configuration.path_to_executable.unknown_version")
    override fun invalidVersionMessage() = PylintBundle.message("pylint.configuration.pylint_invalid_version", PylintPluginPackageManagementService.MINIMUM_VERSION)
    override fun notInstalledMessage() = PylintBundle.message("pylint.configuration.pylint_not_installed", "Pylint")
}