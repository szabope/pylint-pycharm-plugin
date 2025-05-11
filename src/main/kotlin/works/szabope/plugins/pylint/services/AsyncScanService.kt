package works.szabope.plugins.pylint.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import works.szabope.plugins.common.dialog.IDialogManager
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.ScanService
import works.szabope.plugins.common.services.tool.AbstractToolOutputHandler
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.run.PylintCliExecutor
import works.szabope.plugins.pylint.run.PylintSdkExecutor
import works.szabope.plugins.pylint.services.parser.PylintMessage
import works.szabope.plugins.pylint.services.parser.PylintParseException
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import javax.swing.event.HyperlinkEvent

@Service(Service.Level.PROJECT)
class AsyncScanService(private val project: Project, private val cs: CoroutineScope) : ScanService<PylintMessage> {

    private var manualScanJob: Job? = null

    val scanInProgress: Boolean
        get() = manualScanJob?.isActive == true

    override fun scan(
        targets: Collection<VirtualFile>,
        configuration: ImmutableSettingsData,
        resultHandler: AbstractToolOutputHandler<PylintMessage>
    ) {
        manualScanJob = if (configuration.useProjectSdk) {
            cs.launch {
                PylintSdkExecutor(project).execute(configuration, targets, resultHandler)
                    .onFailure { ex -> handleException(configuration, targets, ex) }
            }
        } else {
            cs.launch {
                PylintCliExecutor(project).execute(configuration, targets, resultHandler)
                    .onFailure { ex -> handleException(configuration, targets, ex) }
            }
        }
    }

    private fun handleException(configuration: ImmutableSettingsData, targets: Collection<VirtualFile>, e: Throwable) {
        when (e) {
            is PylintParseException -> {
                showClickableBalloonError(PylintBundle.message("pylint.toolwindow.balloon.parse_error")) {
                    IDialogManager.showToolOutputParseErrorDialog(
                        configuration, targets.joinToString(" "), e.sourceJson, e.message ?: "N/A"
                    )
                }
            }

            is PylintCliExecutor.CommandExecutionException -> {
                showClickableBalloonError(PylintBundle.message("pylint.toolwindow.balloon.external_error")) {
                    IDialogManager.showToolExecutionErrorDialog(
                        e.command, e.stderr, e.statusCode
                    )
                }
            }

            else -> {
                thisLogger().error(PylintBundle.message("pylint.please_report_this_issue"), e)
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
        fun getInstance(project: Project): AsyncScanService = project.service()
    }
}