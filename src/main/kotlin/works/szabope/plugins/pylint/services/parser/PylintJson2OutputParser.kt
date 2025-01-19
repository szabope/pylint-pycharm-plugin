package works.szabope.plugins.pylint.services.parser

import com.intellij.openapi.diagnostic.thisLogger
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class PylintParserException(val sourceJson: String, cause: SerializationException) : RuntimeException(cause)

object PylintJson2OutputParser {

    private val withUnknownKeys = Json { ignoreUnknownKeys = true }

    @Throws(PylintParserException::class)
    fun parse(json: String): PylintResult {
        try {
            val result = withUnknownKeys.decodeFromString<PylintResult>(json)
            adjustForPlatform(result)
            return result
        } catch (e: SerializationException) {
            thisLogger().debug(e)
            throw PylintParserException(json, e)
        }
    }

    /**
     * Adjust line numbers
     *   from pylint: 1-based
     *   to intellij: 0-based
     */
    private fun adjustForPlatform(original: PylintResult) {
        original.messages.forEach { message ->
            message.line -= 1
            message.endLine = message.endLine?.let { it - 1 }
        }
    }
}
