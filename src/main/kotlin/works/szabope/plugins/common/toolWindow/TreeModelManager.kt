package works.szabope.plugins.common.toolWindow

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.treeStructure.Tree
import works.szabope.plugins.common.CommonBundle

class TreeModelManager(private val isDisplayed: (String) -> Boolean) {

    private val changeListeners = mutableSetOf<() -> Unit>()
    private val logger = logger<TreeModelManager>()
    private val issues = mutableSetOf<TreeModelDataItem>()
    private val model = TreeModel(CommonBundle.message("toolwindow.name.empty"))

    fun add(issue: TreeModelDataItem) {
        issues.add(issue)
        if (isDisplayed(issue)) {
            addToTree(issue)
            triggerChangeListeners()
            logger.debug("Issue added to tree: $issue")
        }
    }

    fun reload() {
        resetRoot()
        issues.filter { isDisplayed(it) }.forEach { addToTree(it) }
        triggerChangeListeners()
    }

    fun reinitialize(targets: Collection<VirtualFile>) {
        issues.clear()
        resetRoot(targets)
    }

    fun getRootScanPaths(): Collection<VirtualFile> {
        return model.root.targets
    }

    fun install(tree: Tree) {
        tree.model = model
    }

    fun addChangeListener(listener: () -> Unit) {
        changeListeners.add(listener)
    }

    private fun triggerChangeListeners() {
        changeListeners.forEach { it() }
    }

    private fun resetRoot(targetsMaybe: Collection<VirtualFile>? = null) {
        val targets = targetsMaybe ?: model.root.targets
        model.setRoot(RootNode(CommonBundle.message("toolwindow.root.message", 0, 0), targets))
    }

    private fun addToTree(issue: TreeModelDataItem) {
        val fileNode = findOrAddFileNode(issue.file)
        val issueNode = IssueNode(issue)
        model.append(issueNode, fileNode)
        model.updateRootText(
            CommonBundle.message(
                "toolwindow.root.message", model.getIssueCount(), model.getChildCount(model.root)
            )
        )
    }

    private fun isDisplayed(issue: TreeModelDataItem): Boolean {
        return isDisplayed(issue.severity.level)
    }

    private fun findOrAddFileNode(file: String): StringNode {
        var fileNode = model.findFileNode(file)
        if (fileNode == null) {
            fileNode = StringNode(file)
            model.append(fileNode, model.root)
        }
        return fileNode
    }
}
