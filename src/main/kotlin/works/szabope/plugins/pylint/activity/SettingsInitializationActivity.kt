package works.szabope.plugins.pylint.activity

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.backend.workspace.workspaceModel
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import org.jetbrains.annotations.TestOnly
import works.szabope.plugins.pylint.services.IncompleteConfigurationNotificationService
import works.szabope.plugins.pylint.services.OldPylintSettings
import works.szabope.plugins.pylint.services.PylintPackageUtil
import works.szabope.plugins.pylint.services.PylintSettings

internal class SettingsInitializationActivity : ProjectActivity {

    @TestOnly
    val configurationCalled = Channel<Unit>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)

    override suspend fun execute(project: Project) {
        configurePlugin(project)
        project.workspaceModel.eventLog.filter {
            it.getChanges(ModuleEntity::class.java).isNotEmpty()
        }.collectLatest {
            configurePlugin(project)
        }
    }

    @TestOnly
    suspend fun configurePlugin(project: Project) {
        PylintPackageUtil.reloadPackages(project)
        val settings = PylintSettings.getInstance(project)
        if (!settings.isComplete()) {
            settings.initSettings(OldPylintSettings.getInstance(project))
        }
        if (!settings.isComplete()) {
            val notificationService = IncompleteConfigurationNotificationService.getInstance(project)
            val canInstall = PylintPackageUtil.canInstall(project)
            notificationService.notify(canInstall)
        }
        configurationCalled.send(Unit)
    }
}
