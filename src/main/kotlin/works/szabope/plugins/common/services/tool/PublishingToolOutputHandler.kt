package works.szabope.plugins.common.services.tool

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.common.messages.TreeListener
import works.szabope.plugins.common.services.ToolResult
import works.szabope.plugins.common.services.ToolResultItem
import works.szabope.plugins.common.toolWindow.TreeModelDataItem

abstract class PublishingToolOutputHandler<I : ToolResultItem, R : ToolResult<I>>(private val project: Project) :
    AbstractToolOutputHandler<I, R>() {

    abstract fun convert(message: I): TreeModelDataItem

    override suspend fun handleResult(message: I) {
        val item = convert(message)
        withContext(Dispatchers.EDT) {
            project.messageBus.syncPublisher(TreeListener.TOPIC).add(item)
        }
    }

    override suspend fun handle(result: R) {
        super.handle(result)
        ActivityTracker.getInstance().inc()
    }
}
