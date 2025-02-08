package works.szabope.plugins.pylint.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowManager
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.pyi.PyiFileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.dialog.IDialogManager
import works.szabope.plugins.pylint.run.ExecutorConfiguration
import works.szabope.plugins.pylint.run.PylintCliExecutor
import works.szabope.plugins.pylint.run.PylintCliExecutor.ParseFailedException
import works.szabope.plugins.pylint.run.PylintSdkExecutor
import works.szabope.plugins.pylint.services.parser.CollectingOutputHandler
import works.szabope.plugins.pylint.services.parser.PublishingOutputHandler
import works.szabope.plugins.pylint.services.parser.PylintMessage
import works.szabope.plugins.pylint.services.parser.PylintParserException
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import javax.swing.event.HyperlinkEvent

@Service(Service.Level.PROJECT)
class PylintService(private val project: Project, private val cs: CoroutineScope) {

    private val logger = logger<PylintService>()
    private var manualScanJob: Job? = null

    val scanInProgress: Boolean
        get() = manualScanJob?.isActive == true

    @Suppress("UnstableApiUsage")
    fun scan(targets: List<String>, configuration: ExecutorConfiguration): List<PylintMessage> {
        val resultHandler = CollectingOutputHandler()
        if (configuration.useProjectSdk) {
            runBlockingCancellable {
                PylintSdkExecutor(project).execute(configuration, targets, resultHandler)
            }
        } else {
            try {
                runBlockingCancellable { PylintCliExecutor(project).execute(configuration, targets, resultHandler) }
            } catch (e: PylintParserException) {
                logger.warn(PylintBundle.message("pylint.executable.parsing-result-failed", e))
            } catch (e: PylintCliExecutor.CommandExecutionException) {
                logger.warn(
                    PylintBundle.message(
                        "pylint.executable.error", e.command, e.statusCode, e.stderr
                    )
                )
            }
        }
        return resultHandler.getResults()
    }

    fun scanAsync(targets: List<String>, configuration: ExecutorConfiguration) {
        val resultHandler = PublishingOutputHandler(project)
        if (configuration.useProjectSdk) {
            manualScanJob = cs.launch {
                PylintSdkExecutor(project).execute(configuration, targets, resultHandler)
            }
        } else {
            manualScanJob = cs.launch {
                try {
                    PylintCliExecutor(project).execute(configuration, targets, resultHandler)
                } catch (e: ParseFailedException) {
                    showClickableBalloonError(PylintBundle.message("pylint.toolwindow.balloon.parse_error")) {
                        IDialogManager.showPylintParseErrorDialog(
                            e.command, e.sourceJson, e.cause?.message ?: "N/A"
                        )
                    }
                } catch (e: PylintCliExecutor.CommandExecutionException) {
                    showClickableBalloonError(PylintBundle.message("pylint.toolwindow.balloon.external_error")) {
                        IDialogManager.showPylintExecutionErrorDialog(
                            e.command, e.stderr, e.statusCode
                        )
                    }
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

    companion object {
        @JvmStatic
        val SUPPORTED_FILE_TYPES = arrayOf(PythonFileType.INSTANCE, PyiFileType.INSTANCE)

        @JvmStatic
        fun getInstance(project: Project): PylintService = project.service()
    }
}
