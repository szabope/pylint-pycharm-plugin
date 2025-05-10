package works.szabope.plugins.pylint.services.parser

import kotlinx.coroutines.flow.Flow
import works.szabope.plugins.common.services.tool.CollectingToolOutputHandler

class PylintCollectingToolOutputHandler : CollectingToolOutputHandler<PylintMessage>() {
    override suspend fun parse(stdout: Flow<String>): Result<Collection<PylintMessage>> {
        val fullStdOut = buildString { stdout.collect { append(it) } }
        return PylintJson2OutputParser.parse(fullStdOut)
    }
}