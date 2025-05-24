package works.szabope.plugins.pylint.toolWindow

import works.szabope.plugins.common.services.SeverityConfig
import works.szabope.plugins.common.toolWindow.TreeModelDataItem

data class PylintTreeModelDataItem(
    override val file: String,
    override val line: Int,
    override val column: Int,
    override val message: String,
    override val code: String,
    override val severity: SeverityConfig,
) : TreeModelDataItem {
    override fun toRepresentation() = "[$code] $message"
}