package works.szabope.plugins.common.services.tool

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.common.messages.MessageConverter
import works.szabope.plugins.common.services.ToolResultItem
import works.szabope.plugins.common.toolWindow.ITreeService
import works.szabope.plugins.common.toolWindow.TreeModelDataItem

abstract class PublishingToolOutputHandler<I : ToolResultItem>(
    private val project: Project, private val converter: MessageConverter<I, TreeModelDataItem>
) : AbstractToolOutputHandler<I>() {

    private fun convert(message: I): TreeModelDataItem = converter.convert(message)

    override suspend fun handleResult(message: I) {
        val item = convert(message)
        withContext(Dispatchers.EDT) {
            ITreeService.getInstance(project).add(item)
        }
    }
}
