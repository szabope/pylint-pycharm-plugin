package works.szabope.plugins.pylint.services.cli

import com.intellij.openapi.project.Project
import com.intellij.python.terminal.PyVirtualEnvTerminalCustomizer
import com.intellij.util.EnvironmentUtil

class PythonEnvironmentAwareCli(private val project: Project) {

    suspend fun execute(command: String, workDir: String? = null): Cli.Status {
        require(command.isNotBlank())
        val environment = getEnvironment().toMutableMap()
        val environmentAwareCommand = PyVirtualEnvTerminalCustomizer().customizeCommandAndEnvironment(
            project, project.basePath, command.split(" ").toTypedArray(), environment
        ).filter { it.isNotEmpty() }.joinToString(" ")
        return Cli.execute(environmentAwareCommand, workDir, environment)
    }

    private fun getEnvironment(): Map<String, String> {
        val envs = HashMap(System.getenv())
        envs[EnvironmentUtil.DISABLE_OMZ_AUTO_UPDATE] = "true"
        envs["HISTFILE"] = "/dev/null"
        return envs
    }
}
