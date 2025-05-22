@file:Suppress("UnstableApiUsage")

package works.szabope.plugins.pylint.testutil

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.ActionUtil
import org.junit.Assert
import works.szabope.plugins.pylint.action.RescanAction
import works.szabope.plugins.pylint.action.ScanAction
import works.szabope.plugins.pylint.action.StopScanAction

fun scan(context: DataContext) {
    val action = ActionUtil.getAction(ScanAction.ID)!! as ScanAction
    val actionEvent = AnActionEvent.createEvent(context, null, ActionPlaces.EDITOR_TAB, ActionUiKind.NONE, null)
    action.update(actionEvent)
    Assert.assertTrue(actionEvent.presentation.isEnabled)
    action.actionPerformed(actionEvent)
}

fun rescan(context: DataContext) {
    val action = ActionUtil.getAction(RescanAction.ID)!! as RescanAction
    val actionEvent = AnActionEvent.createEvent(context, null, ActionPlaces.EDITOR_TAB, ActionUiKind.NONE, null)
    action.update(actionEvent)
    Assert.assertTrue(actionEvent.presentation.isEnabled)
    action.actionPerformed(actionEvent)
}

fun stopScan(context: DataContext) {
    val action = ActionUtil.getAction(StopScanAction.ID)!! as StopScanAction
    val actionEvent = AnActionEvent.createEvent(context, null, ActionPlaces.EDITOR_TAB, ActionUiKind.NONE, null)
    action.update(actionEvent)
    Assert.assertTrue(actionEvent.presentation.isEnabled)
    action.actionPerformed(actionEvent)
}