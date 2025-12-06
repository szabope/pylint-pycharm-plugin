package works.szabope.plugins.pylint.services.parser

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object PylintOutputParser {

    private val withUnknownKeys = Json { ignoreUnknownKeys = true }

    // https://github.com/Kotlin/kotlinx.serialization/issues/2808
    @Suppress("PROVIDED_RUNTIME_TOO_LOW", "INLINE_CLASSES_NOT_SUPPORTED")
    @Serializable
    data class PylintResult(
        val messages: List<PylintMessage>
    )

    @Throws(SerializationException::class)
    fun parse(json: String): List<PylintMessage> {
        return withUnknownKeys.decodeFromString<PylintResult>(json).messages.map {
            adjustForPlatform(it)
        }
    }

    /**
     * Adjust line numbers
     *   from pylint: 1-based
     *   to intellij: 0-based
     */
    private fun adjustForPlatform(message: PylintMessage): PylintMessage {
        return message.copy(
            type = message.type,
            message = message.message,
            messageId = message.messageId,
            symbol = message.symbol,
            confidence = message.confidence,
            module = message.module,
            path = message.path,
            absolutePath = message.absolutePath,
            line = message.line - 1,
            endLine = message.endLine?.let { it - 1 },
            column = message.column,
            endColumn = message.endColumn,
            obj = message.obj,
        )
    }
}
