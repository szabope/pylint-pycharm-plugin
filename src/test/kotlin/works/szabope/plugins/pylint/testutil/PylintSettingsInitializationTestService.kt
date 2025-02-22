package works.szabope.plugins.pylint.testutil

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import works.szabope.plugins.pylint.activity.SettingsInitializationActivity

@Service
class PylintSettingsInitializationTestService(private val project: Project) {

    private var initializationActivity =
        (StartupActivity.POST_STARTUP_ACTIVITY.point as Sequence<*>).filter { it is SettingsInitializationActivity }
            .single() as SettingsInitializationActivity

    suspend fun triggerReconfiguration() {
        initializationActivity.configurationCalled.tryReceive() // clear existing
        initializationActivity.configurePlugin(project)
        initializationActivity.configurationCalled.receive()
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<PylintSettingsInitializationTestService>()
    }
}