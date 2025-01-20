package works.szabope.plugins.pylint.toolWindow

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