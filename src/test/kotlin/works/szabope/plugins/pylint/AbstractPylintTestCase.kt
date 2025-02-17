package works.szabope.plugins.pylint

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.clearAllMocks
import io.mockk.unmockkAll

abstract class AbstractPylintTestCase : BasePlatformTestCase() {

    override fun tearDown() {
        clearAllMocks()
        unmockkAll()
        super.tearDown()
    }

    protected suspend fun triggerReconfiguration() {
        PylintSettingsInitializationTestService.getInstance(project).triggerReconfiguration()
    }
}