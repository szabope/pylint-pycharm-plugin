package works.szabope.plugins.pylint.toolWindow

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.treeStructure.Tree
import works.szabope.plugins.pylint.PylintBundle

class TreeModelManager(private val displayedSeverityLevels: MutableSet<String>) {
    private val logger = logger<TreeModelManager>()
    private val issues = mutableSetOf<TreeModelDataItem>()
    private val model = TreeModel(PylintBundle.message("pylint.toolwindow.name.empty"))

    fun add(issue: TreeModelDataItem) {
        issues.add(issue)
        if (isDisplayed(issue)) {
            addToTree(issue)
            logger.debug("Issue added to tree: $issue")
        }
    }

    fun reload() {
        resetRoot()
        issues.filter { isDisplayed(it) }.forEach { addToTree(it) }
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

    private fun resetRoot(targetsMaybe: Collection<VirtualFile>? = null) {
        val targets = targetsMaybe ?: model.root.targets
        model.setRoot(RootNode(PylintBundle.message("pylint.toolwindow.root.message", 0, 0), targets))
    }

    private fun addToTree(issue: TreeModelDataItem) {
        val fileNode = findOrAddFileNode(issue.file)
        val issueNode = IssueNode(issue)
        model.append(issueNode, fileNode)
        model.updateRootText(
            PylintBundle.message(
                "pylint.toolwindow.root.message", model.getIssueCount(), model.getChildCount(model.root)
            )
        )
    }

    private fun isDisplayed(issue: TreeModelDataItem): Boolean {
        return displayedSeverityLevels.contains(issue.severity.level)
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
