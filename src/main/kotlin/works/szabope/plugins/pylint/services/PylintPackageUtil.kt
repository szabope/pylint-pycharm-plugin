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
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.sdk.pythonSdk
import works.szabope.plugins.pylint.PylintBundle

object PylintPackageUtil {

    private val PACKAGE = PythonSimplePackageSpecification(
        "pylint", PylintBundle.message("pylint.compatibleVersion"), null, PyRequirementRelation.COMPATIBLE
    )

    fun canInstall(project: Project): Boolean {
        val sdk = getSdk(project) ?: return false
        return !PythonSdkUtil.isRemote(sdk) && !isInstalled(project)
    }

    fun isLocalEnvironment(project: Project): Boolean {
        val sdk = getSdk(project) ?: return false
        return PythonSdkUtil.isVirtualEnv(sdk) || PythonSdkUtil.isCondaVirtualEnv(sdk)
    }

    private fun getPackageManager(project: Project): PythonPackageManager? {
        return getSdk(project)?.let { PythonPackageManager.forSdk(project, it) }
    }

    private fun getSdk(project: Project): Sdk? {
        return project.pythonSdk
    }

    private fun isInstalled(project: Project): Boolean { //TODO: version check + inform user about version compatibility issue
        return getPackageManager(project)?.installedPackages?.any { it.name == PACKAGE.name } ?: false
    }

    suspend fun install(project: Project): PackageManagementService.ErrorDescription? {
        val packageManager = getPackageManager(project)!!
        try {
            packageManager.installPackage(PACKAGE, emptyList())
        } catch (ex: PyExecutionException) {
            return PyPackageManagementService.toErrorDescription(listOf(ex), getSdk(project), PACKAGE.name)
        }
        return null
    }
}
