package works.szabope.plugins.pylint.run

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.nullize
import kotlinx.coroutines.flow.flow
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.tool.ToolOutputHandler
import works.szabope.plugins.pylint.PylintArgs
import works.szabope.plugins.pylint.services.Exclusions
import works.szabope.plugins.pylint.services.cli.PythonEnvironmentAwareCli

class PylintCliExecutor(private val project: Project) : IPylintExecutor {

    class CommandExecutionException(val command: String, val statusCode: Int, val stderr: String) :
        RuntimeException(statusCode.toString())

    override suspend fun execute(
        configuration: ImmutableSettingsData, targets: Collection<VirtualFile>, resultHandler: ToolOutputHandler
    ): Result<Unit> {
        require(!configuration.useProjectSdk) { "Configuration mismatch" }
        val command = buildCommand(configuration, targets)
        val cliResult = PythonEnvironmentAwareCli(project).execute(command = command, configuration.projectDirectory)
        if (cliResult.resultCode != 0) {
            return Result.failure(
                CommandExecutionException(
                    command.joinToString(" "), cliResult.resultCode, cliResult.stderr
                )
            )
        }
        return resultHandler.handle(flow { emit(cliResult.stdout) })
    }

    private fun buildCommand(configuration: ImmutableSettingsData, targets: Collection<VirtualFile>) =
        with(configuration) {
            val command = mutableListOf(executablePath!!)
            configFilePath.nullize(true)?.apply { command.add("--rcfile"); command.add(this) }
            arguments.nullize(true)?.apply { command.addAll(split(" ")) }
            if (excludeNonProjectFiles) {
                Exclusions(project).findAll(targets).joinToString(",").nullize()
                    ?.apply { command.add("--ignore-paths"); command.add(this) }
            }
            // in case of duplicated arguments, latter one wins
            command.addAll(PylintArgs.PYLINT_MANDATORY_COMMAND_ARGS.split(" "))
            command.addAll(targets.map { requireNotNull(it.canonicalPath) })
            command.toTypedArray()
        }
}