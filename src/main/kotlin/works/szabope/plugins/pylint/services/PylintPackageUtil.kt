@file:Suppress("removal", "UnstableApiUsage")

package works.szabope.plugins.pylint.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.webcore.packaging.PackageManagementService
import com.jetbrains.python.packaging.PyExecutionException
import com.jetbrains.python.packaging.common.PythonSimplePackageSpecification
import com.jetbrains.python.packaging.management.PythonPackageManager
import com.jetbrains.python.packaging.requirement.PyRequirementRelation
import com.jetbrains.python.packaging.ui.PyPackageManagementService
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.sdk.pythonSdk

object PylintPackageUtil {

    val minimumVersion = LanguageLevel.PYTHON30

    private val PACKAGE = PythonSimplePackageSpecification(
        "pylint", minimumVersion.toPythonVersion(), null, PyRequirementRelation.COMPATIBLE
    )

    fun canInstall(project: Project): Boolean {
        val sdk = getSdk(project) ?: return false
        return !PythonSdkUtil.isRemote(sdk) && !isInstalled(project)
    }

    fun isLocalEnvironment(project: Project): Boolean {
        val sdk = getSdk(project) ?: return false
        return PythonSdkUtil.isVirtualEnv(sdk) || PythonSdkUtil.isCondaVirtualEnv(sdk)
    }

    fun isSupportedVersionInstalled(project: Project): Boolean {
        val version = getInstalledVersion(project) ?: return false
        return isVersionSupported(version)
    }

    suspend fun reloadPackages(project: Project) = getPackageManager(project)?.reloadPackages()

    fun getInstalledVersion(project: Project): String? {
        return getPackageManager(project)?.installedPackages?.firstOrNull { it.name == PACKAGE.name }?.version
    }

    fun isVersionSupported(version: String): Boolean {
        return LanguageLevel.fromPythonVersion(version)?.isAtLeast(minimumVersion) ?: false
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
            packageManager.installPackage(PACKAGE, emptyList())
        } catch (ex: PyExecutionException) {
            return PyPackageManagementService.toErrorDescription(listOf(ex), getSdk(project), PACKAGE.name)
        }
        return null
    }
}
