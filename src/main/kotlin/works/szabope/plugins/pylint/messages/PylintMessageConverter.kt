package works.szabope.plugins.pylint.messages

import works.szabope.plugins.pylint.services.parser.PylintMessage
import works.szabope.plugins.pylint.toolWindow.SeverityConfig
import works.szabope.plugins.pylint.toolWindow.TreeModelDataItem

class PylintMessageConverter : MessageConverter<PylintMessage, TreeModelDataItem> {
    override fun convert(source: PylintMessage): TreeModelDataItem {
        return with(source) {
            val severity = requireNotNull(SeverityConfig.find(type)) {
                "Pylint message with type '$type' is not supported. Please, report this issue at  https://github.com/szabope/pylint-pycharm-plugin/issues"
            }
            TreeModelDataItem(absolutePath, line, column, message, symbol, severity)
        }
    }
}