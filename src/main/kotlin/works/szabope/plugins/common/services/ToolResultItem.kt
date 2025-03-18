package works.szabope.plugins.common.services

interface ToolResultItem

interface ToolResult<out I: ToolResultItem> {
    val messages: Collection<I>
}