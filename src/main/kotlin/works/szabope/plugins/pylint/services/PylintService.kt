package works.szabope.plugins.pylint.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.run.ExecutorConfiguration
import works.szabope.plugins.pylint.run.PylintCliExecutor
import works.szabope.plugins.pylint.run.PylintSdkExecutor
import works.szabope.plugins.pylint.services.parser.CollectingOutputHandler
import works.szabope.plugins.pylint.services.parser.PylintMessage
import works.szabope.plugins.pylint.services.parser.PylintParserException

@Service(Service.Level.PROJECT)
class PylintService(private val project: Project) {

    private val logger = logger<PylintService>()

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

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PylintService = project.service()
    }
}
