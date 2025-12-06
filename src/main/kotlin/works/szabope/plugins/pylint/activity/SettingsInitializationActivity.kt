package works.szabope.plugins.pylint.activity

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import works.szabope.plugins.pylint.services.IncompleteConfigurationNotifier
import works.szabope.plugins.pylint.services.OldPylintSettings
import works.szabope.plugins.pylint.services.PylintPluginPackageManagementService
import works.szabope.plugins.pylint.services.PylintSettings

class SettingsInitializationActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (project.isDefault) {
            return
        }
        if (!ApplicationManager.getApplication().isUnitTestMode) {
            PylintPluginPackageManagementService.getInstance(project).reloadPackages()
        }
        val settings = PylintSettings.getInstance(project)
        // we trust in old settings validity
        settings.initSettings(OldPylintSettings.getInstance(project))
        if (settings.getValidConfiguration().isFailure) {
            val canInstall = PylintPluginPackageManagementService.getInstance(project).canInstall()
            IncompleteConfigurationNotifier.notify(project, canInstall)
        }
    }
}
