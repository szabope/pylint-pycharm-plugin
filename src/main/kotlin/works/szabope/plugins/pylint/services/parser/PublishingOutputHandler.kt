package works.szabope.plugins.pylint.services.parser

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.common.messages.TreeListener
import works.szabope.plugins.pylint.messages.PylintMessageConverter

class PublishingOutputHandler(private val project: Project) : AbstractOutputHandler() {

    private val converter = PylintMessageConverter(project)

    override suspend fun handleResult(message: PylintMessage) {
        val item = converter.convert(message)
        withContext(Dispatchers.EDT) {
            project.messageBus.syncPublisher(TreeListener.TOPIC).add(item)
        }
    }

    override suspend fun handle(result: PylintResult) {
        super.handle(result)
        ActivityTracker.getInstance().inc()
    }
}
