package works.szabope.plugins.pylint

import com.intellij.openapi.progress.withCurrentThreadCoroutineScopeBlocking
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.HeavyPlatformTestCase
import com.intellij.util.ThrowableRunnable
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import com.intellij.testFramework.replaceService
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.pylint.action.PylintScanJobRegistryService
import works.szabope.plugins.pylint.services.PylintPluginPackageManagementService
import works.szabope.plugins.pylint.testutil.PylintPluginPackageManagementServiceStub

abstract class AbstractPylintHeavyPlatformTestCase : HeavyPlatformTestCase() {

    // local variables are not supported in mockk answer, yet
    private lateinit var pylintPackageManagementServiceStub: AbstractPluginPackageManagementService

    override fun setUp() {
        VfsRootAccess.allowRootAccess(testRootDisposable, "/usr/bin")
        mockkObject(PylintPluginPackageManagementService.Companion)
        every { PylintPluginPackageManagementService.getInstance(any(Project::class)) } answers {
            if (!::pylintPackageManagementServiceStub.isInitialized) {
                pylintPackageManagementServiceStub = PylintPluginPackageManagementServiceStub(
                    firstArg<Project>()
                )
            }
            pylintPackageManagementServiceStub
        }
        super.setUp()
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
}