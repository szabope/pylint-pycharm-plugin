package works.szabope.plugins.pylint.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.future.future
import kotlinx.serialization.SerializationException
import works.szabope.plugins.common.run.ToolExecutionTerminatedException
import works.szabope.plugins.common.services.ToolExecutorConfiguration
import works.szabope.plugins.common.services.showClickableBalloonError
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.dialog.DialogManager
import works.szabope.plugins.pylint.services.parser.PylintMessage
import works.szabope.plugins.pylint.services.parser.PylintOutputParser
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel

@Service(Service.Level.PROJECT)
class ScanService(private val project: Project, private val cs: CoroutineScope) {

    fun scan(targets: Collection<VirtualFile>, configuration: ToolExecutorConfiguration): List<PylintMessage> =
        cs.future { scanAsync(targets, configuration) }.get()

    suspend fun scanAsync(targets: Collection<VirtualFile>, configuration: ToolExecutorConfiguration): List<PylintMessage> {
        val parameters = with(project) { buildParamList(configuration, targets) }
        val stdErr = StringBuilder()
        val stdOut = PylintExecutor(project).execute(configuration, parameters).filter { it.text.isNotBlank() }
            .transform { if (it.isError) stdErr.append(it.text) else emit(it) }.catch {
                if (it is ToolExecutionTerminatedException) {
                    showClickableBalloonError(
                        project, PylintToolWindowPanel.ID, PylintBundle.message("pylint.toolwindow.balloon.external_error")
                    ) {
                        DialogManager.showToolExecutionErrorDialog(
                            configuration, stdErr.toString(), it.exitCode
                        )
                    }
                } else {
                    // Unexpected exception - tool likely gone
                    PylintIncompleteConfigurationNotifier.getInstance(project).showWarningBubble(false)
                }
            }.map { it.text }.toList().joinToString("\n")
        return try {
            PylintOutputParser.parse(stdOut)
        } catch (it: SerializationException) {
            // Unexpected exception
            showClickableBalloonError(
                project, PylintToolWindowPanel.ID, PylintBundle.message("pylint.toolwindow.balloon.failed_to_execute")
            ) {
                DialogManager.showToolOutputParseErrorDialog(
                    configuration, targets.joinToString { it.path }, stdOut, it.message ?: "N/A"
                )
            }
            emptyList()
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ScanService = project.service()
    }
}
