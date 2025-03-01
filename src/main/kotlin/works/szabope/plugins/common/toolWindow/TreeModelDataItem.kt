package works.szabope.plugins.common.toolWindow

import works.szabope.plugins.common.services.SeverityConfig

data class TreeModelDataItem(
    val file: String,
    val line: Int,
    val column: Int,
    val message: String,
    val code: String,
    val severity: SeverityConfig
) {
    fun toRepresentation(): String = "[$code] $message"
}