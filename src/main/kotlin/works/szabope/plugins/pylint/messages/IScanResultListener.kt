package works.szabope.plugins.pylint.messages

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import works.szabope.plugins.pylint.toolWindow.TreeModelDataItem

interface IScanResultListener {

    fun reinitialize(targets: Collection<VirtualFile>)
    fun add(item: TreeModelDataItem)

    companion object {
        @JvmStatic
        @ProjectLevel
        val TOPIC = Topic(IScanResultListener::class.java, Topic.BroadcastDirection.NONE)
    }
}
