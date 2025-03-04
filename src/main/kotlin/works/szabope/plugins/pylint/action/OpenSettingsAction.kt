package works.szabope.plugins.pylint.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.common.configurable.GeneralConfigurable

class OpenSettingsAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        ToolWindowManager.getInstance(e.project ?: return).invokeLater {
            ShowSettingsUtil.getInstance().showSettingsDialog(
                e.project, GeneralConfigurable::class.java
            )
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.OpenSettingsAction"
    }
}