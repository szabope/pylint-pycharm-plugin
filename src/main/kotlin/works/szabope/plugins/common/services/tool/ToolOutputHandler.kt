package works.szabope.plugins.common.services.tool

import kotlinx.coroutines.flow.Flow

interface ToolOutputHandler {
    suspend fun handle(stdout: Flow<String>): Result<Unit>
}
