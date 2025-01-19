package works.szabope.plugins.pylint.messages

import com.intellij.util.messages.MessageBus
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import works.szabope.plugins.pylint.services.parser.PylintMessage

class ScanResultPublisher(messageBus: MessageBus) {

    private val publisher = messageBus.syncPublisher(SCAN_RESULT_TOPIC)

    fun publish(message: PylintMessage) {
        publisher.process(message)
    }

    companion object {
        @JvmStatic
        @ProjectLevel
        val SCAN_RESULT_TOPIC = Topic(IScanResultListener::class.java, Topic.BroadcastDirection.NONE)
    }
}
