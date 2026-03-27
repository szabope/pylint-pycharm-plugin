package works.szabope.plugins.pylint.testutil

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.test.toolWindow.AbstractTestToolWindowHeadlessManagerImpl
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel

class TestToolWindowHeadlessManagerImpl(project: Project) :
    AbstractTestToolWindowHeadlessManagerImpl(project) {

    override val toolWindowId = PylintToolWindowPanel.ID
}
