package works.szabope.plugins.pylint.toolWindow

import com.intellij.openapi.project.Project
import works.szabope.plugins.pylint.PylintBundle

class PylintToolWindowFactory : MyToolWindowFactory(PylintBundle.message("pylint.toolwindow.name")) {
    override fun createPanel(project: Project) = PylintToolWindowPanel(project)
}
