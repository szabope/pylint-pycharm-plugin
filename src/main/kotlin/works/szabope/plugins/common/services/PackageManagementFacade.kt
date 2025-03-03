@file:Suppress("removal", "UnstableApiUsage")

package works.szabope.plugins.common.services

import com.intellij.openapi.project.Project
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

open class PackageManagementFacade(private val requirement: PyRequirement) {

    fun canInstall(project: Project): Boolean {
        val sdk = getSdk(project) ?: return false
        return !PythonSdkUtil.isRemote(sdk) && !isInstalled(project)
    }

    fun isLocalEnvironment(project: Project): Boolean {
        val sdk = getSdk(project) ?: return false
        return PythonSdkUtil.isVirtualEnv(sdk) || PythonSdkUtil.isCondaVirtualEnv(sdk)
    }

    suspend fun reloadPackages(project: Project) = try {
        getPackageManager(project)?.reloadPackages()
    } catch (e: Exception) {
        // e.g. org.apache.hc.client5.http.HttpHostConnectException thrown when docker (in given SDK) is unavailable
        Result.failure(e)
    }

    fun getInstalledVersion(project: Project): Version? {
        return getPackageManager(project)?.installedPackages?.firstOrNull { it.name == requirement.name }?.version?.let {
            Version.parseVersion(
                it
            )
        }
    }

    fun isVersionSupported(version: Version): Boolean {
        return requirement.match(
            mutableListOf(
                PyPackage(
                    "pylint",
                    "${version.major}.${version.minor}.${version.bugfix}"
                )
            )
        ) != null
    }

    private fun getPackageManager(project: Project): PythonPackageManager? {
        return getSdk(project)?.let { PythonPackageManager.forSdk(project, it) }
    }

    private fun getSdk(project: Project): Sdk? {
        return project.pythonSdk
    }

    private fun isInstalled(project: Project): Boolean {
        return getInstalledVersion(project) != null
    }

    suspend fun install(project: Project): PackageManagementService.ErrorDescription? {
        if (isInstalled(project)) return null
        val packageManager = getPackageManager(project)!!
        try {
            packageManager.installPackage(
                PythonSimplePackageSpecification(requirement.name, null, null, null),
                emptyList()
            ).getOrThrow()
        } catch (ex: PyExecutionException) {
            return PyPackageManagementService.toErrorDescription(listOf(ex), getSdk(project), requirement.name)
        }
        return null
    }
}
