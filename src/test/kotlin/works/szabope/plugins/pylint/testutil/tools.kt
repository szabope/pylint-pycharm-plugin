package works.szabope.plugins.pylint.testutil

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.junit.Assert
import works.szabope.plugins.pylint.action.RescanAction
import works.szabope.plugins.pylint.action.ScanAction
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel

@Suppress("UnstableApiUsage")
fun scan(file: VirtualFile, project: Project) {
    val action = ActionUtil.getAction(ScanAction.ID)!! as ScanAction
    val dataContext = SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project)
        .add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(file)).build()
    val actionEvent = AnActionEvent.createEvent(dataContext, null, ActionPlaces.EDITOR_TAB, ActionUiKind.NONE, null)
    action.update(actionEvent)
    Assert.assertTrue(actionEvent.presentation.isEnabled)
    action.actionPerformed(actionEvent)
}

@Suppress("UnstableApiUsage")
fun rescan(project: Project, panel: PylintToolWindowPanel) {
    val action = ActionUtil.getAction(RescanAction.ID)!! as RescanAction
    val dataContext = SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project)
        .add(PylintToolWindowPanel.PYLINT_PANEL_DATA_KEY, panel).build()
    val actionEvent = AnActionEvent.createEvent(dataContext, null, ActionPlaces.EDITOR_TAB, ActionUiKind.NONE, null)
    action.update(actionEvent)
    Assert.assertTrue(actionEvent.presentation.isEnabled)
    action.actionPerformed(actionEvent)
}

