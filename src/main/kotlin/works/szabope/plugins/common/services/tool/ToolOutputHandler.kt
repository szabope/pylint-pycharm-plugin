package works.szabope.plugins.common.services.tool

import works.szabope.plugins.common.services.ToolResult
import works.szabope.plugins.common.services.ToolResultItem

interface ToolOutputHandler<in I : ToolResultItem> {
    suspend fun handle(result: ToolResult<I>)
}
