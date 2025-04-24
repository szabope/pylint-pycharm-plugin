@file:Suppress("UnstableApiUsage")

package works.szabope.plugins.common.services

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.util.Version
import com.intellij.webcore.packaging.PackageManagementService
import com.jetbrains.python.packaging.PyExecutionException
import com.jetbrains.python.packaging.PyPackage
import com.jetbrains.python.packaging.PyRequirement
import com.jetbrains.python.packaging.common.PythonSimplePackageSpecification
import com.jetbrains.python.packaging.management.PythonPackageManager
import com.jetbrains.python.packaging.ui.PyPackageManagementService
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.sdk.pythonSdk

@Suppress("removal")
abstract class PackageManagementFacade : IPackageManagementFacade {

    override fun canInstall(): Boolean {
        val sdk = getSdk() ?: return false
        return !PythonSdkUtil.isRemote(sdk) && !isInstalled()
    }

    override fun isLocalEnvironment(): Boolean {
        val sdk = getSdk() ?: return false
        return PythonSdkUtil.isVirtualEnv(sdk) || PythonSdkUtil.isCondaVirtualEnv(sdk)
    }

    override suspend fun reloadPackages() = try {
        getPackageManager()?.reloadPackages()
    } catch (e: Exception) {
        // e.g. org.apache.hc.client5.http.HttpHostConnectException thrown when docker (in given SDK) is unavailable
        Result.failure(e)
    }

    override fun getInstalledVersion(): Version? {
        return getPackageManager()?.installedPackages?.firstOrNull { it.name == getRequirement().name }?.version?.let {
            Version.parseVersion(
                it
            )
        }
    }

    override fun isVersionSupported(version: Version): Boolean {
        return getRequirement().match(
            mutableListOf(
                PyPackage(
                    getRequirement().name, "${version.major}.${version.minor}.${version.bugfix}"
                )
            )
        ) != null
    }

    override fun getPackageManager(): PythonPackageManager? {
        return getSdk()?.let { PythonPackageManager.forSdk(project, it) }
    }

    override fun getSdk(): Sdk? {
        return project.pythonSdk
    }

    override fun isInstalled(): Boolean {
        return getInstalledVersion() != null
    }

    override suspend fun installRequirement(): PackageManagementService.ErrorDescription? {
        val packageManager = getPackageManager()!!
        val requirement = getRequirement()
        val versionSpec = requirement.versionSpecs.firstOrNull()
        val specification = PythonSimplePackageSpecification(
            requirement.name, versionSpec?.version, null, versionSpec?.relation
        )
        try {
            packageManager.installPackage(specification, options = emptyList()).getOrThrow()
        } catch (ex: PyExecutionException) {
            return PyPackageManagementService.toErrorDescription(listOf(ex), getSdk(), requirement.name)
        }
        return null
    }

    protected abstract fun getRequirement(): PyRequirement
}
