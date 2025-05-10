package works.szabope.plugins.pylint.services.parser

import kotlinx.serialization.Serializable
import works.szabope.plugins.common.services.ToolResultItem

// https://github.com/Kotlin/kotlinx.serialization/issues/2808
@Suppress("PROVIDED_RUNTIME_TOO_LOW", "INLINE_CLASSES_NOT_SUPPORTED")
@Serializable
data class PylintMessage(
    val type: String,
    val message: String,
    val messageId: String,
    val symbol: String,
    val confidence: String,
    val module: String,
    val path: String,
    val absolutePath: String,
    var line: Int,
    var endLine: Int?,
    val column: Int,
    val endColumn: Int?,
    val obj: String
) : ToolResultItem
