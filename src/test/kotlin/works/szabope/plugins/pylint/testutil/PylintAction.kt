package works.szabope.plugins.pylint.testutil

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.ActionUtil
import works.szabope.plugins.pylint.action.InstallPylintAction

object PylintAction {
    @JvmStatic
    fun installPylint(context: DataContext) {
        val installAction = ActionUtil.wrap(InstallPylintAction.ID)
        val event = AnActionEvent.createEvent(context, null, ActionPlaces.NOTIFICATION, ActionUiKind.NONE, null)
        ActionUtil.invokeAction(installAction, event) {}
    }
}
