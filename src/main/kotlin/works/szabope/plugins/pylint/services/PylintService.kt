package works.szabope.plugins.pylint.services

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputType
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.util.text.nullize
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.pyi.PyiFileType
import com.jetbrains.python.sdk.pythonSdk
import kotlinx.coroutines.*
import works.szabope.plugins.pylint.PylintArgs
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.dialog.IDialogManager
import works.szabope.plugins.pylint.run.PylintConfigurationType
import works.szabope.plugins.pylint.run.PylintRunner
import works.szabope.plugins.pylint.services.cli.PythonEnvironmentAwareCli
import works.szabope.plugins.pylint.services.parser.*
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import javax.swing.event.HyperlinkEvent
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.io.path.Path

@Service(Service.Level.PROJECT)
class PylintService(private val project: Project, private val cs: CoroutineScope) {

    private val logger = logger<PylintService>()
    private val pylintRunner = PylintRunner()
    private var manualScanJob: Job? = null

    val scanInProgress: Boolean
        get() = manualScanJob?.isActive == true

    class MyRunConfiguration( //TODO: name it right
        val executablePath: String,
        val useProjectSdk: Boolean,
        val configFilePath: String? = null,
        val arguments: String? = null,
        val excludeNonProjectFiles: Boolean = true,
        val customExclusions: List<String> = listOf(),
        val projectDirectory: String
    )

    class CommandExecutionException(val statusCode: Int, val stderr: String) : RuntimeException(statusCode.toString())

    @Suppress("UnstableApiUsage")
    fun scan(filePaths: List<String>, runConfiguration: MyRunConfiguration): List<PylintMessage> {
        val command = buildCommand(runConfiguration, filePaths)
        val handler = CollectingOutputHandler()
        try {
            runBlockingCancellable { execute(command = command, runConfiguration.projectDirectory, handler) }
            return handler.getResults()
        } catch (e: PylintParserException) {
            logger.warn(PylintBundle.message("pylint.executable.parsing-result-failed", e))
        } catch (e: CommandExecutionException) {
            logger.warn(
                PylintBundle.message(
                    "pylint.executable.error", command.joinToString(" "), e.statusCode, e.stderr
                )
            )
        }
        return emptyList()
    }

    fun scanAsync(scanPaths: List<String>, runConfiguration: MyRunConfiguration) {
        val command = buildCommand(runConfiguration, scanPaths)
        val handler = PublishingOutputHandler(project)

        if (runConfiguration.useProjectSdk) {
            val configurationFactory = PylintConfigurationType.INSTANCE.getFactory()
            val conf = configurationFactory.createConfiguration(project, "pylint")
            val workDir = project.basePath!!
            conf.sdk = project.pythonSdk
            conf.workingDirectory = workDir
            conf.setAddContentRoots(true)
            conf.setAddSourceRoots(true)
            conf.scriptName = "pylint"
            conf.scriptParameters = command.sliceArray(1..<command.size).joinToString(" ")
            conf.isModuleMode = true
            conf.collectOutputFromProcessHandler()
            val settings = RunManager.getInstance(project).createConfiguration(conf, configurationFactory)
            // as RunnerAndConfigurationSettingsImpl
            settings.isActivateToolWindowBeforeRun = false
            val executor = DefaultRunExecutor.getRunExecutorInstance()
            val environment = ExecutionEnvironmentBuilder.create(executor, settings).runner(pylintRunner).build()
            ProgramRunnerUtil.executeConfigurationAsync(environment, false, false) {
                if (it.processHandler != null) {
                    manualScanJob = cs.launch {
                        it.processHandler?.collectOutput { _, outputType -> outputType == ProcessOutputType.STDOUT }
                            ?.let { stdout -> PylintJson2OutputParser.parse(stdout) }?.apply { handler.handle(this) }
                    }
                }
            }
        } else {
            manualScanJob = cs.launch {
                try {
                    execute(command = command, runConfiguration.projectDirectory, handler)
                } catch (e: PylintParserException) {
                    showClickableBalloonError(PylintBundle.message("pylint.toolwindow.balloon.parse_error")) {
                        IDialogManager.showPylintParseErrorDialog(
                            command.joinToString(" "), e.sourceJson, e.cause?.message ?: "N/A"
                        )
                    }
                } catch (e: CommandExecutionException) {
                    showClickableBalloonError(PylintBundle.message("pylint.toolwindow.balloon.external_error")) {
                        IDialogManager.showPylintExecutionErrorDialog(
                            command.joinToString(" "), e.stderr, e.statusCode
                        )
                    }
                }
            }
        }
    }

    private suspend fun ProcessHandler.collectOutput(handler: (event: ProcessEvent, outputType: Key<*>) -> Boolean): String =
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
                    if (handler(event, outputType)) {
                        event.text?.let(stdout::append)
                    }
                }
            })
        }

    fun cancelScan() {
        cs.launch {
            manualScanJob?.cancelAndJoin()
        }
    }

    private fun showClickableBalloonError(balloonMessage: String, onClick: () -> Unit) {
        ToolWindowManager.getInstance(project).notifyByBalloon(
            PylintToolWindowPanel.ID, MessageType.ERROR, balloonMessage, null
        ) {
            if (it.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                onClick()
            }
        }
    }

    private fun buildCommand(runConfiguration: MyRunConfiguration, targets: List<String>) = with(runConfiguration) {
        val command = mutableListOf(executablePath)
        configFilePath.nullize(true)?.apply { command.add("--rcfile"); command.add("\"$this\"") }
        arguments.nullize(true)?.apply { command.addAll(split(" ")) }
        if (excludeNonProjectFiles) {
            targets.flatMap { collectExclusionsFor(it) }.union(customExclusions).joinToString(",").nullize()
                ?.apply { command.add("--ignore-paths"); command.add("\"$this\"") }
        }
        // in case of duplicated arguments, latter one wins
        command.addAll(PylintArgs.PYLINT_MANDATORY_COMMAND_ARGS.split(" "))
        command.addAll(targets.map { "\"$it\"" })
        command.toTypedArray()
    }

    private fun collectExclusionsFor(target: String): List<String> {
        val exclusions = mutableListOf<String>()
        val workspaceModel = WorkspaceModel.getInstance(project)
        val targetUrl = workspaceModel.getVirtualFileUrlManager().fromPath(target)
        workspaceModel.currentSnapshot.getVirtualFileUrlIndex().findEntitiesByUrl(targetUrl).forEach { entity ->
            if (entity is ContentRootEntity) {
                entity.excludedUrls.mapNotNull { it.url.virtualFile?.path }.map { Path(it) }
                    .forEach { exclusions.add(it.toCanonicalPath()) }
            }
        }
        return exclusions
    }


    private suspend fun execute(vararg command: String, workDir: String, stdoutHandler: IPylintOutputHandler) {
        val cliResult = PythonEnvironmentAwareCli(project).execute(command = command, workDir)
        if (cliResult.resultCode != 0) {
            throw CommandExecutionException(cliResult.resultCode, cliResult.stderr)
        }
        val pylintResult = PylintJson2OutputParser.parse(cliResult.stdout)
        stdoutHandler.handle(pylintResult)
    }

    companion object {
        @JvmStatic
        val SUPPORTED_FILE_TYPES = arrayOf(PythonFileType.INSTANCE, PyiFileType.INSTANCE)

        @JvmStatic
        fun getInstance(project: Project): PylintService = project.service()
    }
}
