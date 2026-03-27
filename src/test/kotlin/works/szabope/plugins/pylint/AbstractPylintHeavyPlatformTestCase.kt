package works.szabope.plugins.pylint

import com.intellij.openapi.project.Project
import com.intellij.testFramework.replaceService
import io.mockk.every
import io.mockk.mockkObject
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.common.test.AbstractPluginHeavyPlatformTestCase
import works.szabope.plugins.pylint.action.PylintScanJobRegistryService
import works.szabope.plugins.pylint.services.PylintPluginPackageManagementService
import works.szabope.plugins.pylint.testutil.PylintPluginPackageManagementServiceStub

abstract class AbstractPylintHeavyPlatformTestCase : AbstractPluginHeavyPlatformTestCase() {

    override fun setupPackageManagementServiceMock(stubProvider: (Project) -> AbstractPluginPackageManagementService) {
        mockkObject(PylintPluginPackageManagementService.Companion)
        every { PylintPluginPackageManagementService.getInstance(any(Project::class)) } answers {
            stubProvider(firstArg())
        }
    }

    override fun createPackageManagementServiceStub(project: Project) = PylintPluginPackageManagementServiceStub(project)

    override fun onSetUp() {
        project.replaceService(PylintScanJobRegistryService::class.java, PylintScanJobRegistryService(), testRootDisposable)
    }
}
