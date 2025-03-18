package works.szabope.plugins.common.services

interface ToolResultItem

interface ToolResult<I: ToolResultItem> {
    val messages: Collection<I>
}