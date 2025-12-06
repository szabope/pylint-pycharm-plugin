package works.szabope.plugins.pylint.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import javax.swing.event.HyperlinkEvent

fun showClickableBalloonError(project: Project, balloonMessage: String, onClick: () -> Unit) {
    ToolWindowManager.getInstance(project).notifyByBalloon(
        PylintToolWindowPanel.ID, MessageType.ERROR, balloonMessage, null
    ) {
        if (it.eventType == HyperlinkEvent.EventType.ACTIVATED) {
            onClick()
        }
    }
}