package works.szabope.plugins.pylint

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking

abstract class PylintTestCase : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        PylintSettingsInitializationTestService.getInstance(project).executeInitialization()
    }

    protected fun awaitProcessed(cb: () -> Unit) = runBlocking {
        PylintSettingsInitializationTestService.getInstance(project).awaitProcessed(cb)
    }
}