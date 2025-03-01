package works.szabope.plugins.common.services.tool

import works.szabope.plugins.common.services.ToolResult
import works.szabope.plugins.common.services.ToolResultItem

abstract class AbstractToolOutputHandler<I : ToolResultItem, R : ToolResult<I>> : ToolOutputHandler<I, R> {

    abstract suspend fun handleResult(message: I)

    override suspend fun handle(result: R) {
        result.messages.forEach { message ->
            handleResult(message)
        }
    }
}
