package works.szabope.plugins.pylint.testutil

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.testFramework.PlatformTestUtil
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.PythonSdkUpdater
import com.jetbrains.python.sdk.getOrCreateAdditionalData
import com.jetbrains.python.sdk.pythonSdk

class MockSdkFactory(private val project: Project, private val module: Module) {

    fun setupWithPath(pathToSdk: String): Sdk {
        val pythonSdk = PythonMockSdk.create(pathToSdk)
        WriteAction.runAndWait<RuntimeException> {
            ProjectJdkTable.getInstance().addJdk(pythonSdk, project)
        }
        val pythonVersion = LanguageLevel.getLatest().toPythonVersion()
        pythonSdk.putUserData(PythonSdkType.MOCK_PY_VERSION_KEY, pythonVersion)
        module.pythonSdk = pythonSdk
        runWriteActionAndWait {
            pythonSdk.getOrCreateAdditionalData()
        }
        PythonSdkUpdater.updateVersionAndPathsSynchronouslyAndScheduleRemaining(pythonSdk, project)
        ApplicationManager.getApplication()
            .invokeAndWait { PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue() }
        return pythonSdk
    }

    fun cleanup(sdk: Sdk) {
        WriteAction.runAndWait<RuntimeException> {
            ProjectJdkTable.getInstance().removeJdk(sdk)
        }
    }
}
