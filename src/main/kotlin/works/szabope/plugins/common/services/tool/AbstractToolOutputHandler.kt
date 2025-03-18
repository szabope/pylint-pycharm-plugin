package works.szabope.plugins.common.services.tool

import kotlinx.coroutines.flow.Flow
import works.szabope.plugins.common.services.ToolResult
import works.szabope.plugins.common.services.ToolResultItem

abstract class AbstractToolOutputHandler<I : ToolResultItem> : ToolOutputHandler<I> {

    override suspend fun handle(stdout: Flow<String>) {
        val items = parse(stdout)
        items.messages.forEach { message ->
            handleResult(message)
        }
    }

    protected abstract suspend fun parse(stdout: Flow<String>): ToolResult<I>

    protected abstract suspend fun handleResult(message: I)
}
