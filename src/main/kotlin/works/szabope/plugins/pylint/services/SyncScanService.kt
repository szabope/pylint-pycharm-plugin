package works.szabope.plugins.pylint.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.ScanService
import works.szabope.plugins.common.services.tool.AbstractToolOutputHandler
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.run.PylintCliExecutor
import works.szabope.plugins.pylint.run.PylintSdkExecutor
import works.szabope.plugins.pylint.services.parser.PylintMessage
import works.szabope.plugins.pylint.services.parser.PylintParseException

@Service(Service.Level.PROJECT)
class SyncScanService(private val project: Project) : ScanService<PylintMessage> {

    private val logger = logger<SyncScanService>()

    @Suppress("UnstableApiUsage")
    override fun scan(
        targets: Collection<VirtualFile>,
        configuration: ImmutableSettingsData,
        resultHandler: AbstractToolOutputHandler<PylintMessage>
    ) {
        if (configuration.useProjectSdk) {
            runBlockingCancellable {
                PylintSdkExecutor(project).execute(configuration, targets, resultHandler)
            }
        } else {
            runBlockingCancellable { PylintCliExecutor(project).execute(configuration, targets, resultHandler) }
        }.onFailure { error ->
            when (error) {
                is PylintParseException -> {
                    logger.error(PylintBundle.message("pylint.executable.parsing-result-failed", configuration), error)
                }

                is PylintCliExecutor.CommandExecutionException -> {
                    logger.error(
                        PylintBundle.message(
                            "pylint.executable.error", error.command, error.statusCode, error.stderr
                        ), error
                    )
                }

                else -> logger.error(PylintBundle.message("pylint.please_report_this_issue"), error)
            }
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): SyncScanService = project.service()
    }
}
