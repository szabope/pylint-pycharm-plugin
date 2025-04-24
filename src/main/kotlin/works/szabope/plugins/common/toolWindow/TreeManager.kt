package works.szabope.plugins.common.toolWindow

import com.intellij.ide.DefaultTreeExpander
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.actionSystem.DataSink
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.UiDataProvider
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil

class TreeManager(val tree: Tree = Tree(), severities: Set<String>) : UiDataProvider {
    private val severityManager = SeverityManager(severities)
    val modelManager = TreeModelManager(severityManager::isSeverityLevelDisplayed)
    private val treeExpander = DefaultTreeExpander(tree)

    init {
        severityManager.addChangeListener {
            modelManager.reload()
        }
        modelManager.install(tree)
    }

    override fun uiDataSnapshot(sink: DataSink) {
        sink[PlatformDataKeys.TREE_EXPANDER] = treeExpander
        sink[SEVERITY_MANAGER] = severityManager
        sink[TREE_MODEL_MANAGER] = modelManager
    }

    fun getSelectedNodeUserObject() = TreeUtil.getLastUserObject(tree.selectionPath)

    companion object {
        @JvmStatic
        val SEVERITY_MANAGER: DataKey<SeverityManager> = DataKey.create("SeverityManager")

        @JvmStatic
        val TREE_MODEL_MANAGER: DataKey<TreeModelManager> = DataKey.create("TreeModelManager")
    }
}