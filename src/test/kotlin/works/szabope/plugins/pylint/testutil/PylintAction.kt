package works.szabope.plugins.pylint.testutil

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.ActionUtil
import org.junit.Assert
import works.szabope.plugins.pylint.action.InstallPylintAction
import works.szabope.plugins.pylint.action.ScanAction

object PylintAction {
    @JvmStatic
    fun installPylint(context: DataContext) {
        val installAction = ActionUtil.wrap(InstallPylintAction.ID)
        val event = AnActionEvent.createEvent(context, null, ActionPlaces.NOTIFICATION, ActionUiKind.NONE, null)
        ActionUtil.invokeAction(installAction, event) {}
    }

    @JvmStatic
    fun tryScan(context: DataContext): AnActionEvent {
        val scanAction = ActionUtil.wrap(ScanAction.ID)
        val event = AnActionEvent.createEvent(context, null, ActionPlaces.EDITOR_TAB, ActionUiKind.NONE, null)
        @Suppress("OverrideOnly")
        scanAction.update(event)
        Assert.assertTrue(event.presentation.isEnabled)
        ActionUtil.invokeAction(scanAction, event) {}
        return event
    }
}
