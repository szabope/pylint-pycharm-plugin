package works.szabope.plugins.common.services.tool

import works.szabope.plugins.common.services.ToolResult
import works.szabope.plugins.common.services.ToolResultItem

abstract class AbstractToolOutputHandler<I : ToolResultItem> : ToolOutputHandler<I> {

    abstract suspend fun handleResult(message: I)

    override suspend fun handle(result: ToolResult<I>) {
        result.messages.forEach { message ->
            handleResult(message)
        }
    }
}
