package works.szabope.plugins.common.messages

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import works.szabope.plugins.common.toolWindow.TreeModelDataItem

interface TreeListener {

    fun reinitialize(targets: Collection<VirtualFile>)
    fun add(item: TreeModelDataItem)

    companion object {
        @JvmStatic
        @ProjectLevel
        val TOPIC = Topic(TreeListener::class.java, Topic.BroadcastDirection.NONE)
    }
}
