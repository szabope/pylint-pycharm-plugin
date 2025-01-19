package works.szabope.plugins.pylint.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.util.text.nullize
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.pyi.PyiFileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import works.szabope.plugins.pylint.PylintArgs
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.dialog.IDialogManager
import works.szabope.plugins.pylint.services.cli.PythonEnvironmentAwareCli
import works.szabope.plugins.pylint.services.parser.*
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import javax.swing.event.HyperlinkEvent
import kotlin.io.path.Path

@Service(Service.Level.PROJECT)
class PylintService(private val project: Project, private val cs: CoroutineScope) {

    private val logger = logger<PylintService>()

    private var manualScanJob: Job? = null

    val scanInProgress: Boolean
        get() = manualScanJob?.isActive == true

    data class RunConfiguration(
        val executablePath: String,
        val configFilePath: String? = null,
        val arguments: String? = null,
        val excludeNonProjectFiles: Boolean = true,
        val customExclusions: List<String> = listOf(),
        val projectDirectory: String
    )

    class CommandExecutionException(val statusCode: Int, val stderr: String) : RuntimeException(statusCode.toString())

    @Suppress("UnstableApiUsage")
    fun scan(filePaths: List<String>, runConfiguration: RunConfiguration): List<PylintMessage> {
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

    fun scanAsync(scanPaths: List<String>, runConfiguration: RunConfiguration) {
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

    private fun buildCommand(runConfiguration: RunConfiguration, targets: List<String>) = with(runConfiguration) {
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
