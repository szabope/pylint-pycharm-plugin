package works.szabope.plugins.pylint.run

import com.intellij.openapi.project.Project
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.util.text.nullize
import works.szabope.plugins.pylint.PylintArgs
import works.szabope.plugins.pylint.services.cli.PythonEnvironmentAwareCli
import works.szabope.plugins.pylint.services.parser.IPylintOutputHandler
import works.szabope.plugins.pylint.services.parser.PylintJson2OutputParser
import works.szabope.plugins.pylint.services.parser.PylintParserException

class PylintCliExecutor(private val project: Project) : AbstractPylintExecutor() {

    class CommandExecutionException(val command: String, val statusCode: Int, val stderr: String) :
        RuntimeException(statusCode.toString())

    class ParseFailedException(val command: String, val sourceJson: String, cause: Exception) : RuntimeException(cause)

    override suspend fun execute(
        configuration: ExecutorConfiguration, targets: List<String>, resultHandler: IPylintOutputHandler
    ) {
        require(!configuration.useProjectSdk) { "Configuration mismatch" }
        val command = buildCommand(configuration, targets)
        val cliResult = PythonEnvironmentAwareCli(project).execute(command = command, configuration.projectDirectory)
        if (cliResult.resultCode != 0) {
            throw CommandExecutionException(command.joinToString(" "), cliResult.resultCode, cliResult.stderr)
        }
        try {
            val pylintResult = PylintJson2OutputParser.parse(cliResult.stdout)
            resultHandler.handle(pylintResult)
        } catch (e: PylintParserException) {
            throw ParseFailedException(command.joinToString(" "), e.sourceJson, e)
        }
    }

    private fun buildCommand(configuration: ExecutorConfiguration, targets: List<String>) = with(configuration) {
        val command = mutableListOf(executablePath)
        configFilePath.nullize(true)?.apply { command.add("--rcfile"); command.add(this) }
        arguments.nullize(true)?.apply { command.addAll(split(" ")) }
        if (excludeNonProjectFiles) {
            val workspaceModel = WorkspaceModel.getInstance(project)
            targets.flatMap { collectExclusionsFor(it, workspaceModel) }.union(customExclusions).joinToString(",")
                .nullize()?.apply { command.add("--ignore-paths"); command.add(this) }
        }
        // in case of duplicated arguments, latter one wins
        command.addAll(PylintArgs.PYLINT_MANDATORY_COMMAND_ARGS.split(" "))
        command.addAll(targets)
        command.toTypedArray()
    }
}