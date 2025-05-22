package works.szabope.plugins.common.toolWindow

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.treeStructure.Tree

abstract class AbstractTreeService(severities: Set<String>) : ITreeService {

    private val modelManager = TreeModelManager(severities)

    override fun getRootScanPaths() = modelManager.getRootScanPaths()

    override fun reinitialize(targets: Collection<VirtualFile>) {
        modelManager.reinitialize(targets)
    }

    override fun addChangeListener(onModelChange: () -> Unit) {
        modelManager.addChangeListener(onModelChange)
    }

    override fun isSeverityLevelDisplayed(level: String) = modelManager.isSeverityLevelDisplayed(level)

    override fun setSeverityLevelDisplayed(level: String, selected: Boolean) {
        modelManager.setSeverityLevelDisplayed(level, selected)
    }

    override fun add(item: TreeModelDataItem) {
        modelManager.add(item)
    }

    override fun install(tree: Tree) {
        modelManager.install(tree)
    }
}