package works.szabope.plugins.pylint

import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.progress.withCurrentThreadCoroutineScopeBlocking
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ThrowableRunnable
import com.jetbrains.python.sdk.pythonSdk
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import com.intellij.testFramework.replaceService
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.common.test.sdk.PythonMockSdk
import works.szabope.plugins.pylint.action.PylintScanJobRegistryService
import works.szabope.plugins.pylint.services.PylintPluginPackageManagementService
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.testutil.PylintPluginPackageManagementServiceStub

abstract class AbstractPylintTestCase : BasePlatformTestCase() {

    // local variables are not supported in mockk answer, yet
    private lateinit var packageManagementServiceStub: AbstractPluginPackageManagementService

    override fun setUp() {
        // FIXME: this is a duct tape for
        //  com.intellij.python.community.services.systemPython.searchPythonsPhysicallyNoCache
        //  accessing /usr/bin/python3(\.\d+)? which is not allowed from tests
        VfsRootAccess.allowRootAccess(testRootDisposable, "/usr/bin")
        mockkObject(PylintPluginPackageManagementService.Companion)
        every { PylintPluginPackageManagementService.getInstance(any(Project::class)) } answers {
            if (!::packageManagementServiceStub.isInitialized) {
                packageManagementServiceStub = PylintPluginPackageManagementServiceStub(
                    firstArg<Project>()
                )
            }
            packageManagementServiceStub
        }
        super.setUp()
        PylintSettings.getInstance(project).reset()
        project.replaceService(PylintScanJobRegistryService::class.java, PylintScanJobRegistryService(), testRootDisposable)
    }

    override fun runTestRunnable(testRunnable: ThrowableRunnable<Throwable>) {
        withCurrentThreadCoroutineScopeBlocking { super.runTestRunnable(testRunnable) }
    }

    override fun tearDown() {
        clearAllMocks()
        unmockkAll()
        super.tearDown()
    }

    /**
     * https://youtrack.jetbrains.com/issue/IJPL-197007
     */
    override fun getProjectDescriptor(): LightProjectDescriptor? {
        return LightProjectDescriptor()
    }

    fun withMockSdk(path: String, action: (Sdk) -> Unit) {
        val mockSdk = PythonMockSdk.create(path)
        runWriteActionAndWait {
            ProjectJdkTable.getInstance().addJdk(mockSdk)
        }
        project.pythonSdk = mockSdk
        module.pythonSdk = mockSdk
        try {
            action(mockSdk)
        } finally {
            project.pythonSdk = null
            module.pythonSdk = null
            runWriteActionAndWait {
                ProjectJdkTable.getInstance().removeJdk(mockSdk)
            }
        }
    }
}