package works.szabope.plugins.pylint.configurable

import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.target.TargetedCommandLineBuilder
import com.intellij.execution.target.local.LocalTargetEnvironment
import com.intellij.execution.target.local.LocalTargetEnvironmentRequest
import com.intellij.openapi.project.Project
import com.jetbrains.python.packaging.PyPackage
import works.szabope.plugins.common.services.PluginPackageManagementException
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.services.PylintPluginPackageManagementService
import java.io.File

class PylintValidator(private val project: Project) {
    fun validateExecutablePath(path: String?): String? {
        val path = path ?: return null
        require(path.isNotBlank())
        val file = File(path)
        if (!file.exists()) {
            return PylintBundle.message("pylint.configuration.path_to_executable.not_exists")
        }
        if (file.isDirectory) {
            return PylintBundle.message("pylint.configuration.path_to_executable.is_directory")
        }
        if (!file.canExecute()) {
            return PylintBundle.message("pylint.configuration.path_to_executable.not_executable")
        }
        return null
    }

    fun validateVersion(path: String): String? {
        val pylintVersion = getVersionForExecutable(path)
            ?: return PylintBundle.message("pylint.configuration.path_to_executable.unknown_version")
        if (!PylintPluginPackageManagementService.getInstance(project).getRequirement()
                .match(PyPackage("pylint", pylintVersion))
        ) {
            return PylintBundle.message(
                "pylint.configuration.pylint_invalid_version", PylintPluginPackageManagementService.MINIMUM_VERSION
            )
        }

        return null
    }

    private fun getVersionForExecutable(pathToExecutable: String): String? {
        val targetEnvRequest = LocalTargetEnvironmentRequest()
        val targetEnvironment = LocalTargetEnvironment(LocalTargetEnvironmentRequest())

        val commandLineBuilder = TargetedCommandLineBuilder(targetEnvRequest)
        commandLineBuilder.setExePath(pathToExecutable)
        commandLineBuilder.addParameters("--version")

        val targetCMD = commandLineBuilder.build()

        val process = targetEnvironment.createProcess(targetCMD)

        return runCatching {
            val processHandler = CapturingProcessHandler(
                process, targetCMD.charset, targetCMD.getCommandPresentation(targetEnvironment)
            )
            val processOutput = processHandler.runProcess(5000, true).stdout
            "(\\d+.\\d+.\\d+)".toRegex().find(processOutput)?.groups?.last()?.value
        }.getOrNull()
    }

    fun validateProjectSdk(): String? {
        PylintPluginPackageManagementService.getInstance(project).checkInstalledRequirement().onFailure {
            when (it) {
                is PluginPackageManagementException.PackageNotInstalledException -> return PylintBundle.message(
                    "pylint.configuration.pylint_not_installed", "Pylint"
                )

                is PluginPackageManagementException.PackageVersionObsoleteException -> return PylintBundle.message(
                    "pylint.configuration.pylint_invalid_version", PylintPluginPackageManagementService.MINIMUM_VERSION
                )
            }
        }
        return null
    }
}