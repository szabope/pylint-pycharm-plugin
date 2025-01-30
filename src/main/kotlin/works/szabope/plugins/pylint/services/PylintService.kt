package works.szabope.plugins.pylint.services

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputType
import com.intellij.execution.runners.ExecutionEnvironment
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
import kotlinx.coroutines.*
import works.szabope.plugins.pylint.PylintArgs
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.dialog.IDialogManager
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

    private var manualScanJob: Job? = null

    val scanInProgress: Boolean
        get() = manualScanJob?.isActive == true

    data class MyRunConfiguration( //TODO: name it right
        val executablePath: String,
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
            runBlockingCancellable { execute(command, runConfiguration.projectDirectory, handler) }
            return handler.getResults()
        } catch (e: PylintParserException) {
            logger.warn(PylintBundle.message("pylint.executable.parsing-result-failed", e))
        } catch (e: CommandExecutionException) {
            logger.warn(PylintBundle.message("pylint.executable.error", command, e.statusCode, e.stderr))
        }
        return emptyList()
    }

    fun scanAsync(scanPaths: List<String>, runConfiguration: MyRunConfiguration) {
        val command = buildCommand(runConfiguration, scanPaths)
        val handler = PublishingOutputHandler(project)
        manualScanJob = cs.launch {
            try {
                execute(command, runConfiguration.projectDirectory, handler)
            } catch (e: PylintParserException) {
                showClickableBalloonError(PylintBundle.message("pylint.toolwindow.balloon.parse_error")) {
                    IDialogManager.showPylintParseErrorDialog(
                        command, e.sourceJson, e.cause?.message ?: "N/A"
                    )
                }
            } catch (e: CommandExecutionException) {
                showClickableBalloonError(PylintBundle.message("pylint.toolwindow.balloon.external_error")) {
                    IDialogManager.showPylintExecutionErrorDialog(
                        command, e.stderr, e.statusCode
                    )
                }
            }
        }
    }

    fun scanWithSdkAsync(environment: ExecutionEnvironment) {
        ProgramRunnerUtil.executeConfigurationAsync(environment, false, false) {
            val handler = it.processHandler
            if (handler != null) {
                cs.launch {
                    val stdout = handler.collectOutput { _, outputType -> outputType == ProcessOutputType.STDOUT }
                    val pylintResult = PylintJson2OutputParser.parse(stdout)
                    PublishingOutputHandler(project).handle(pylintResult)
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
        val commandBuilder = StringBuilder(executablePath)
        configFilePath.nullize(true)?.apply { commandBuilder.append(" --rcfile $this") }
        arguments.nullize(true)?.apply { commandBuilder.append(" $this") }
        if (excludeNonProjectFiles) {
            targets.flatMap { collectExclusionsFor(it) }.union(customExclusions).joinToString(",").nullize()
                ?.apply { commandBuilder.append(" --ignore-paths $this") }
        }
        // in case of duplicated arguments, latter one wins
        commandBuilder.append(" ").append(PylintArgs.PYLINT_MANDATORY_COMMAND_ARGS)
        commandBuilder.append(" ").append(targets.joinToString(" ")).toString()
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


    private suspend fun execute(command: String, workDir: String, stdoutHandler: IPylintOutputHandler) {
        val cliResult = PythonEnvironmentAwareCli(project).execute(command, workDir)
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
