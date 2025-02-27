package works.szabope.plugins.pylint.services.parser

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.pylint.messages.IScanResultListener
import works.szabope.plugins.pylint.messages.PylintMessageConverter

class PublishingOutputHandler(private val project: Project) : AbstractOutputHandler() {

    private val converter = PylintMessageConverter()

    override suspend fun handleResult(result: PylintMessage) {
        val item = converter.convert(result)
        withContext(Dispatchers.EDT) {
            project.messageBus.syncPublisher(IScanResultListener.TOPIC).add(item)
        }
    }

    override suspend fun handle(result: PylintResult) {
        super.handle(result)
        ActivityTracker.getInstance().inc()
    }
}
