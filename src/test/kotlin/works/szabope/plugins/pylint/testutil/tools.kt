package works.szabope.plugins.pylint.testutil

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.psi.PsiFile
import org.junit.Assert
import works.szabope.plugins.pylint.action.ScanAction

@Suppress("UnstableApiUsage")
fun scan(file: PsiFile) {
    val action = ActionUtil.getAction(ScanAction.ID)!! as ScanAction
    val dataContext = SimpleDataContext.builder().add(CommonDataKeys.PROJECT, file.project)
        .add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(file.virtualFile)).build()
    val actionEvent = AnActionEvent.createEvent(dataContext, null, ActionPlaces.EDITOR_TAB, ActionUiKind.NONE, null)
    action.update(actionEvent)
    Assert.assertTrue(actionEvent.presentation.isEnabled)
    action.actionPerformed(actionEvent)
}

