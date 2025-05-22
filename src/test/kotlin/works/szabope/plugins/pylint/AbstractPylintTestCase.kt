package works.szabope.plugins.pylint

import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.python.packaging.management.PythonPackageManager
import com.jetbrains.python.sdk.pythonSdk
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import works.szabope.plugins.common.sdk.PythonMockSdk
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.pylint.testutil.PylintSettingsInitializationTestService
import works.szabope.plugins.pylint.testutil.PythonPackageManagerStub

abstract class AbstractPylintTestCase : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        Settings.getInstance(project).reset()
    }

    override fun tearDown() {
        clearAllMocks()
        unmockkAll()
        super.tearDown()
    }

    protected suspend fun triggerReconfiguration() {
        PylintSettingsInitializationTestService.getInstance(project).triggerReconfiguration()
    }

    @Suppress("UnstableApiUsage")
    fun withMockSdk(path: String, action: (PythonPackageManagerStub) -> Unit) {
        val mockSdk = PythonMockSdk.create(path)
        runWriteActionAndWait {
            ProjectJdkTable.getInstance().addJdk(mockSdk)
        }
        project.pythonSdk = mockSdk
        module.pythonSdk = mockSdk
        val packageManager = PythonPackageManagerStub(project, mockSdk)
        mockkObject(PythonPackageManager)
        every { PythonPackageManager.forSdk(any(), any()) } returns packageManager
        try {
            action(packageManager)
        } finally {
            project.pythonSdk = null
            module.pythonSdk = null
            runWriteActionAndWait {
                ProjectJdkTable.getInstance().removeJdk(mockSdk)
            }
        }
    }
}