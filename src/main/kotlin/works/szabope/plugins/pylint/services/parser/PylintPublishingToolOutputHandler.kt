package works.szabope.plugins.pylint.services.parser

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.services.tool.PublishingToolOutputHandler
import works.szabope.plugins.pylint.messages.PylintMessageConverter

class PylintPublishingToolOutputHandler(project: Project) :
    PublishingToolOutputHandler<PylintMessage>(project, PylintMessageConverter(project))