package works.szabope.plugins.pylint.run

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputType
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.nullize
import com.jetbrains.python.sdk.pythonSdk
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.future.await
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.tool.ToolOutputHandler
import works.szabope.plugins.pylint.PylintArgs
import works.szabope.plugins.pylint.services.Exclusions
import java.util.concurrent.CompletableFuture

class PylintSdkExecutor(private val project: Project) : IPylintExecutor {

    private val configurationFactory = PylintConfigurationType.INSTANCE.getFactory()

    override suspend fun execute(
        configuration: ImmutableSettingsData, targets: Collection<VirtualFile>, resultHandler: ToolOutputHandler
    ): Result<Unit> {
        require(configuration.useProjectSdk) { "Configuration mismatch" }
        val environment = createEnvironment(configuration, targets)
        val futureProcessHandler = CompletableFuture<ProcessHandler>()
        ProgramRunnerUtil.executeConfigurationAsync(environment, false, false) {
            val processHandler = requireNotNull(it.processHandler)
            futureProcessHandler.complete(processHandler)
        }
        val processHandler = futureProcessHandler.await()
        processHandler.collectOutput { outputType -> outputType == ProcessOutputType.STDOUT }.let { stdoutFlow ->
            resultHandler.handle(stdoutFlow).onFailure { ex ->
                return Result.failure(ex)
            }
        }
        return Result.success(Unit)
    }

    private fun createEnvironment(
        configuration: ImmutableSettingsData, targets: Collection<VirtualFile>
    ): ExecutionEnvironment {
        val conf = configurationFactory.createConfiguration(project, "pylint")
        conf.sdk = project.pythonSdk
        conf.workingDirectory = configuration.projectDirectory
        conf.setAddContentRoots(true)
        conf.setAddSourceRoots(true)
        conf.scriptName = "pylint"
        conf.scriptParameters = buildScriptParameters(configuration, targets)
        conf.isModuleMode = true
        conf.collectOutputFromProcessHandler()
        val settings = RunManager.getInstance(project).createConfiguration(conf, configurationFactory)
        settings.isActivateToolWindowBeforeRun = false
        val executor = DefaultRunExecutor.getRunExecutorInstance()
        return ExecutionEnvironmentBuilder.create(executor, settings).runner(PylintRunner.INSTANCE).build()
    }

    private fun buildScriptParameters(configuration: ImmutableSettingsData, targets: Collection<VirtualFile>) =
        with(configuration) {
            val sb = StringBuilder()
            configFilePath.nullize(true)?.apply { sb.append(" --rcfile").append(" \"$this\"") }
            arguments.nullize(true)?.apply { sb.append(" ").append(arguments) }
            if (excludeNonProjectFiles) {
                Exclusions(project).findAll(targets).joinToString(",").nullize()
                    ?.apply { sb.append(" --ignore-paths ").append("\"$this\"") }
            }
            sb.append(" ").append(PylintArgs.PYLINT_MANDATORY_COMMAND_ARGS).append(" ")
            targets.joinToString(" ") { "\"${it.canonicalPath}\"" }.apply { sb.append(this) }
            sb.toString()
        }

    private fun ProcessHandler.collectOutput(handler: (outputType: Key<*>) -> Boolean): Flow<String> = callbackFlow {
        val wholeOutput = StringBuilder()
        addProcessListener(object : ProcessListener {
            override fun startNotified(event: ProcessEvent) {
                event.text?.let(wholeOutput::append)
            }

            override fun processTerminated(event: ProcessEvent) {
                event.text?.let(wholeOutput::append)
                if (event.exitCode == 0) {
                    close()
                } else {
                    throw IllegalStateException("\n=== CONSOLE ===\n$wholeOutput\n=== CONSOLE END ===")
                }
            }

            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                event.text?.let(wholeOutput::append)
                if (handler(outputType)) {
                    event.text?.let { trySend(it) }
                }
            }
        })
        awaitClose()
    }
}
