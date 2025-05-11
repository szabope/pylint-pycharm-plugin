package works.szabope.plugins.common.services.tool

import works.szabope.plugins.common.services.ToolResultItem

abstract class CollectingToolOutputHandler<I : ToolResultItem> : AbstractToolOutputHandler<I>() {
    private val results = mutableListOf<I>()

    override suspend fun handleResult(message: I) {
        results.add(message)
    }

    fun getResults() = results.toList()
}
