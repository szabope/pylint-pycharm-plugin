package works.szabope.plugins.pylint.messages

import com.intellij.ide.ui.IdeUiService
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import works.szabope.plugins.pylint.toolWindow.TreeModelDataItem

@Suppress("UnstableApiUsage")
class PylintResultListener(project: Project) : IScanResultListener {

    private var uiContext: DataContext =
        PylintToolWindowPanel.getInstance(project).let { IdeUiService.getInstance().createUiDataContext(it) }

    override fun reinitialize(targets: Collection<VirtualFile>) {
        requireNotNull(uiContext.getData(PylintToolWindowPanel.TREE_MODEL_MANAGER)) {
            "todo" // TODO
        }.reinitialize(targets)
    }

    override fun add(item: TreeModelDataItem) {
        requireNotNull(uiContext.getData(PylintToolWindowPanel.TREE_MODEL_MANAGER)) {
            "todo" // TODO
        }.add(item)
    }
}
