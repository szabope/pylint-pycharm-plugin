package works.szabope.plugins.common.toolWindow

import com.intellij.ide.DefaultTreeExpander
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import org.jetbrains.annotations.VisibleForTesting
import works.szabope.plugins.pylint.services.pylintSeverityConfigs

@Service(Service.Level.PROJECT)
class TreeManager {

    @VisibleForTesting
    val tree: Tree = Tree()

    private val severities = pylintSeverityConfigs.keys
    private val severityManager = SeverityManager(severities)
    private val modelManager = TreeModelManager(severityManager::isSeverityLevelDisplayed)
    val treeExpander = DefaultTreeExpander(tree)

    init {
        severityManager.addChangeListener {
            modelManager.reload()
        }
        modelManager.install(tree)
    }

    fun getSelectedNodeUserObject() = TreeUtil.getLastUserObject(tree.selectionPath) as? IssueNodeUserObject?

    fun getRootScanPaths() = modelManager.getRootScanPaths()

    fun reinitialize(targets: Collection<VirtualFile>) {
        modelManager.reinitialize(targets)
    }

    fun addChangeListener(onModelChange: () -> Unit) {
        modelManager.addChangeListener(onModelChange)
    }

    fun isSeverityLevelDisplayed(level: String) = severityManager.isSeverityLevelDisplayed(level)

    fun setSeverityLevelDisplayed(level: String, selected: Boolean) {
        severityManager.setSeverityLevelDisplayed(level, selected)
    }

    fun add(item: TreeModelDataItem) {
        modelManager.add(item)
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): TreeManager = project.service()
    }
}