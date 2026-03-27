package works.szabope.plugins.pylint.testutil

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.test.context.dataContext
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel

fun dataContext(
    project: Project, customizer: SimpleDataContext.Builder.() -> Unit
): DataContext = dataContext(project, PylintToolWindowPanel.ID, customizer)
