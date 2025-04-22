package works.szabope.plugins.pylint.messages

import works.szabope.plugins.common.messages.MessageConverter
import works.szabope.plugins.common.toolWindow.TreeModelDataItem
import works.szabope.plugins.pylint.services.parser.PylintMessage
import works.szabope.plugins.pylint.services.pylintSeverityConfigs

object PylintMessageConverter : MessageConverter<PylintMessage, TreeModelDataItem> {
    override fun convert(message: PylintMessage): TreeModelDataItem {
        val severity = requireNotNull(pylintSeverityConfigs[message.type]) {
            """Pylint message with type '${message.type}' is not supported. Please, report this issue at  
                    |https://github.com/szabope/pylint-pycharm-plugin/issues""".trimMargin()
        }
        return with(message) {
            TreeModelDataItem(absolutePath, line, column, this.message, symbol, severity)
        }
    }
}