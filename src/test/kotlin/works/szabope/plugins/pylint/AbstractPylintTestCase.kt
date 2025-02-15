package works.szabope.plugins.pylint

import com.intellij.testFramework.fixtures.BasePlatformTestCase

abstract class AbstractPylintTestCase : BasePlatformTestCase() {

    protected suspend fun triggerReconfiguration() {
        PylintSettingsInitializationTestService.getInstance(project).triggerReconfiguration()
    }
}