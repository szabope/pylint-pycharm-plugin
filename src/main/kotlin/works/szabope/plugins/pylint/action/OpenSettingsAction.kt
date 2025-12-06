package works.szabope.plugins.pylint.action

import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.pylint.configurable.PylintConfigurable

class OpenSettingsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        ToolWindowManager.getInstance(project).invokeLater {
            e.getData(Notification.KEY)?.expire()
            ShowSettingsUtil.getInstance().showSettingsDialog(project, PylintConfigurable::class.java)
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.OpenSettingsAction"
    }
}