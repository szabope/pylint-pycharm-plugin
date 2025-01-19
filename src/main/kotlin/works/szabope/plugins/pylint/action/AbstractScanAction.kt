package works.szabope.plugins.pylint.action

import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import works.szabope.plugins.pylint.services.PylintService
import works.szabope.plugins.pylint.services.PylintSettings

abstract class AbstractScanAction : DumbAwareAction() {
    protected fun isReadyToScan(project: Project): Boolean {
        return PylintSettings.getInstance(project).isComplete() && !PylintService.getInstance(project).scanInProgress
    }
}
