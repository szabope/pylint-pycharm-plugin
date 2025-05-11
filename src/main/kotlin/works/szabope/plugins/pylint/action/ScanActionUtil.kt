package works.szabope.plugins.pylint.action

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.pylint.services.AsyncScanService

object ScanActionUtil {
    fun isReadyToScan(project: Project): Boolean {
        return Settings.getInstance(project).isComplete() && !AsyncScanService.getInstance(project).scanInProgress
    }
}
