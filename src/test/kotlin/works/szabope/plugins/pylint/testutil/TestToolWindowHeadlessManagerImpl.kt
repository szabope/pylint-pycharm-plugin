package works.szabope.plugins.pylint.testutil

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowBalloonShowOptions
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import org.junit.Assert.assertNull
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel

class TestToolWindowHeadlessManagerImpl(project: Project) : ToolWindowHeadlessManagerImpl(project) {
    private val myHandlers = hashMapOf<String, (ToolWindowBalloonShowOptions) -> Unit>()

    override fun notifyByBalloon(options: ToolWindowBalloonShowOptions) {
        myHandlers[options.toolWindowId]?.invoke(options)
    }

    fun onBalloon(handler: (ToolWindowBalloonShowOptions) -> Unit) {
        assertNull(myHandlers.put(PylintToolWindowPanel.ID, handler))
    }

    fun cleanup() {
        myHandlers.clear()
    }
}
