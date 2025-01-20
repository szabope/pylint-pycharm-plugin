package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * This is a placeholder.
 * See init of [PylintToolWindowPanel.addToolbar][works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel.addToolbar]
 */
class ScrollToSourceDummyAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        // intentionally left blank
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.ScrollToSourceAction"
    }
}