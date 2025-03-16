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
import kotlinx.coroutines.future.await
import kotlinx.coroutines.suspendCancellableCoroutine
import works.szabope.plugins.common.services.tool.ToolOutputHandler
import works.szabope.plugins.pylint.PylintArgs
import works.szabope.plugins.pylint.services.Exclusions
import works.szabope.plugins.pylint.services.ExecutorConfiguration
import works.szabope.plugins.pylint.services.parser.PylintJson2OutputParser
import works.szabope.plugins.pylint.services.parser.PylintMessage
import works.szabope.plugins.pylint.services.parser.PylintResult
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PylintSdkExecutor(private val project: Project) : IPylintExecutor {

    private val configurationFactory = PylintConfigurationType.INSTANCE.getFactory()

    override suspend fun execute(
        configuration: ExecutorConfiguration,
        targets: Collection<VirtualFile>,
        resultHandler: ToolOutputHandler<PylintMessage, PylintResult>
    ) {
        require(configuration.useProjectSdk) { "Configuration mismatch" }
        val environment = createEnvironment(configuration, targets)
        val futureProcessHandler = CompletableFuture<ProcessHandler>()
        ProgramRunnerUtil.executeConfigurationAsync(environment, false, false) {
            val processHandler = requireNotNull(it.processHandler)
            futureProcessHandler.complete(processHandler)
        }
        val processHandler = futureProcessHandler.await()
        processHandler.collectOutput { outputType -> outputType == ProcessOutputType.STDOUT }.let { stdout ->
            val result = PylintJson2OutputParser.parse(stdout)
            resultHandler.handle(result)
        }
    }

    private fun createEnvironment(
        configuration: ExecutorConfiguration,
        targets: Collection<VirtualFile>
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

    private fun buildScriptParameters(configuration: ExecutorConfiguration, targets: Collection<VirtualFile>) =
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

    private suspend fun ProcessHandler.collectOutput(handler: (outputType: Key<*>) -> Boolean): String =
        suspendCancellableCoroutine { continuation ->
            val wholeOutput = StringBuilder()
            val stdout = StringBuilder()
            addProcessListener(object : ProcessListener {
                override fun startNotified(event: ProcessEvent) {
                    event.text?.let(wholeOutput::append)
                }

                override fun processTerminated(event: ProcessEvent) {
                    event.text?.let(wholeOutput::append)
                    if (event.exitCode == 0) {
                        continuation.resume(stdout.toString())
                    } else {
                        continuation.resumeWithException(IllegalStateException("\n=== CONSOLE ===\n$wholeOutput\n=== CONSOLE END ==="))
                    }
                }

                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    event.text?.let(wholeOutput::append)
                    if (handler(outputType)) {
                        event.text?.let(stdout::append)
                    }
                }
            })
        }
}