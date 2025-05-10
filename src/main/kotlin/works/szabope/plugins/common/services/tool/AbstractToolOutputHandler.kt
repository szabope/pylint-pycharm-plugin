package works.szabope.plugins.common.services.tool

import kotlinx.coroutines.flow.Flow
import works.szabope.plugins.common.services.ToolResultItem

abstract class AbstractToolOutputHandler<I : ToolResultItem> : ToolOutputHandler {

    override suspend fun handle(stdout: Flow<String>): Result<Unit> {
        val items = parse(stdout).onFailure { return Result.failure(it) }.getOrDefault(emptyList())
        items.forEach { message ->
            handleResult(message)
        }
        return Result.success(Unit)
    }

    protected abstract suspend fun parse(stdout: Flow<String>): Result<Collection<I>>

    protected abstract suspend fun handleResult(message: I)
}
