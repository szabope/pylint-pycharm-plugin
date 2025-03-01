package works.szabope.plugins.pylint.messages

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.messages.MessageConverter
import works.szabope.plugins.common.toolWindow.TreeModelDataItem
import works.szabope.plugins.pylint.services.PylintSeverityConfigService
import works.szabope.plugins.pylint.services.parser.PylintMessage

class PylintMessageConverter(private val project: Project) : MessageConverter<PylintMessage, TreeModelDataItem> {
    override fun convert(message: PylintMessage): TreeModelDataItem {
        val severity = PylintSeverityConfigService.getInstance(project).findByType(message.type) {
            """Pylint message with type '${message.type}' is not supported. Please, report this issue at  
                    |https://github.com/szabope/pylint-pycharm-plugin/issues""".trimMargin()
        }
        return with(message) {
            TreeModelDataItem(absolutePath, line, column, this.message, symbol, severity)
        }
    }
}