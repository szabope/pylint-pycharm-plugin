package works.szabope.plugins.pylint.services.parser

interface ToolMessage {
    val message: String
    val line: Int
    val column: Int
}