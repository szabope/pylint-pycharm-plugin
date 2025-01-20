package works.szabope.plugins.pylint.services.parser

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.pylint.messages.ScanResultPublisher

class PublishingOutputHandler(private val project: Project) : AbstractOutputHandler() {

    override suspend fun handleResult(result: PylintMessage) {
        withContext(Dispatchers.EDT) {
            ScanResultPublisher(project.messageBus).publish(result)
        }
    }

    override suspend fun handle(result: PylintResult) {
        super.handle(result)
        ActivityTracker.getInstance().inc()
    }
}
