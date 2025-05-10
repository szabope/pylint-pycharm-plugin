package works.szabope.plugins.pylint.services.parser

import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.Flow
import works.szabope.plugins.common.services.tool.PublishingToolOutputHandler
import works.szabope.plugins.pylint.messages.PylintMessageConverter

class PylintPublishingToolOutputHandler(project: Project) :
    PublishingToolOutputHandler<PylintMessage>(project, PylintMessageConverter) {
    override suspend fun parse(stdout: Flow<String>): Result<Collection<PylintMessage>> {
        val fullStdOut = buildString { stdout.collect { append(it) } }
        return PylintJson2OutputParser.parse(fullStdOut)
    }
}
