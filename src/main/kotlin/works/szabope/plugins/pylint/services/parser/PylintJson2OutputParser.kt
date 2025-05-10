package works.szabope.plugins.pylint.services.parser

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class PylintParserException(val sourceJson: String, cause: SerializationException) :
    SerializationException(sourceJson, cause)

object PylintJson2OutputParser {

    private val withUnknownKeys = Json { ignoreUnknownKeys = true }

    // https://github.com/Kotlin/kotlinx.serialization/issues/2808
    @Suppress("PROVIDED_RUNTIME_TOO_LOW", "INLINE_CLASSES_NOT_SUPPORTED")
    @Serializable
    data class PylintResult(
        val messages: List<PylintMessage>
    )

    fun parse(json: String): Result<Collection<PylintMessage>> {
        try {
            val messages = withUnknownKeys.decodeFromString<PylintResult>(json).messages
            adjustForPlatform(messages)
            return Result.success(messages)
        } catch (e: SerializationException) {
            return Result.failure(e)
        }
    }

    /**
     * Adjust line numbers
     *   from pylint: 1-based
     *   to intellij: 0-based
     */
    private fun adjustForPlatform(original: Collection<PylintMessage>) {
        original.forEach { message ->
            message.line -= 1
            message.endLine = message.endLine?.let { it - 1 }
        }
    }
}
