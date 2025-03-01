package works.szabope.plugins.common.services.tool

import works.szabope.plugins.common.services.ToolResult
import works.szabope.plugins.common.services.ToolResultItem

class CollectingToolOutputHandler<I : ToolResultItem, R : ToolResult<I>> : AbstractToolOutputHandler<I, R>() {
    private val results = mutableListOf<I>()

    override suspend fun handleResult(message: I) {
        results.add(message)
    }

    fun getResults() = results.toList()
}
