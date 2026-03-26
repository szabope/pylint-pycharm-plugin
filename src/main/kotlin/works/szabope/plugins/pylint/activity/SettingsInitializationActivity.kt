package works.szabope.plugins.pylint.activity

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.activity.AbstractSettingsInitializationActivity
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.common.services.BasicSettingsData
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.pylint.services.PylintIncompleteConfigurationNotifier
import works.szabope.plugins.pylint.services.OldPylintSettings
import works.szabope.plugins.pylint.services.PylintPluginPackageManagementService
import works.szabope.plugins.pylint.services.PylintSettings

class SettingsInitializationActivity : AbstractSettingsInitializationActivity() {

    override fun getPackageManagementService(project: Project): AbstractPluginPackageManagementService =
        PylintPluginPackageManagementService.getInstance(project)

    override fun getSettings(project: Project): Settings = PylintSettings.getInstance(project)

    override suspend fun getOldSettings(project: Project): BasicSettingsData = OldPylintSettings.getInstance(project)

    override fun notifyIncomplete(project: Project, canInstall: Boolean) =
        PylintIncompleteConfigurationNotifier.getInstance(project).showWarningBubble(canInstall)
}
