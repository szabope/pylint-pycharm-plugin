package works.szabope.plugins.pylint.testutil

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil.performAction
import org.junit.Assert
import works.szabope.plugins.common.test.action.updateActionForTest
import works.szabope.plugins.pylint.action.InstallPylintAction
import works.szabope.plugins.pylint.action.ScanAction
import works.szabope.plugins.pylint.action.StopScanAction

fun scan(context: DataContext) {
    val action = ActionManager.getInstance().getAction(ScanAction.ID)
    val event = AnActionEvent.createEvent(context, null, "", ActionUiKind.NONE, null)
    updateActionForTest(action, event)
    Assert.assertTrue(event.presentation.isEnabled)
    performAction(action, event)
}

fun stopScan(context: DataContext) {
    val action = ActionManager.getInstance().getAction(StopScanAction.ID)
    val event = AnActionEvent.createEvent(context, null, ActionPlaces.EDITOR_TAB, ActionUiKind.NONE, null)
    updateActionForTest(action, event)
    Assert.assertTrue(event.presentation.isEnabled)
    performAction(action, event)
}

fun installPylint(context: DataContext) {
    val action = ActionManager.getInstance().getAction(InstallPylintAction.ID)
    val event = AnActionEvent.createEvent(context, null, ActionPlaces.NOTIFICATION, ActionUiKind.NONE, null)
    updateActionForTest(action, event)
    Assert.assertTrue(event.presentation.isEnabled)
    performAction(action, event)
}
