package works.szabope.plugins.common.services.tool

import works.szabope.plugins.common.services.ToolResult
import works.szabope.plugins.common.services.ToolResultItem

interface ToolOutputHandler<I : ToolResultItem, R : ToolResult<I>> {
    suspend fun handle(result: R)
}
