package works.szabope.plugins.common.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.util.Version
import com.intellij.webcore.packaging.PackageManagementService
import com.jetbrains.python.packaging.common.PythonPackage
import com.jetbrains.python.packaging.management.PythonPackageManager

interface IPackageManagementFacade {
    val project: Project
    fun canInstall(): Boolean
    fun isLocalEnvironment(): Boolean

    suspend fun reloadPackages(): Result<List<PythonPackage>>?
    fun getInstalledVersion(): Version?
    fun isVersionSupported(version: Version): Boolean
    fun getPackageManager(): PythonPackageManager?
    fun getSdk(): Sdk?
    fun isInstalled(): Boolean

    suspend fun install(): PackageManagementService.ErrorDescription?
}