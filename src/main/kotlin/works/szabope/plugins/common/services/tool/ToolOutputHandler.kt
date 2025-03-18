package works.szabope.plugins.common.services.tool

import kotlinx.coroutines.flow.Flow
import works.szabope.plugins.common.services.ToolResultItem

interface ToolOutputHandler<in I : ToolResultItem> {
    suspend fun handle(stdout: Flow<String>)
}
