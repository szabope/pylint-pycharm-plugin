package works.szabope.plugins.pylint.services

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.run.ToolExecutor

class PylintExecutor(project: Project) : ToolExecutor(project, "pylint")
