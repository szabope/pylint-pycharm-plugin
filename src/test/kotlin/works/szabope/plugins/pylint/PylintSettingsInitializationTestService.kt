package works.szabope.plugins.pylint

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.pylint.activity.SettingsInitializationActivity

@Service
class PylintSettingsInitializationTestService(private val project: Project, private val cs: CoroutineScope) {

    private var job: Job? = null
    private val initializationActivity = SettingsInitializationActivity()

    suspend fun awaitProcessed(cb: () -> Unit) {
        initializationActivity.configurationCalled.tryReceive() // clear existing
        cb.invoke()
        awaitActivity()
    }

    private suspend fun awaitActivity() {
        initializationActivity.configurationCalled.receive()
    }

    fun executeInitialization() {
        job?.cancel()
        job = cs.launch { initializationActivity.execute(project) }
        runBlocking { awaitActivity() }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<PylintSettingsInitializationTestService>()
    }
}