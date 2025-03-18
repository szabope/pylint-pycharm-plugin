package works.szabope.plugins.pylint.services.parser

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.services.tool.PublishingToolOutputHandler
import works.szabope.plugins.pylint.messages.PylintMessageConverter

// TODO: pass converter as ctr arg
class PylintPublishingToolOutputHandler(project: Project) : PublishingToolOutputHandler<PylintMessage>(project) {
    private val converter = PylintMessageConverter(project)
    override fun convert(message: PylintMessage) = converter.convert(message)
}