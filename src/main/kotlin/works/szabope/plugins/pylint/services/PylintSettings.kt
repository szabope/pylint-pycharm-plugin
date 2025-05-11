package works.szabope.plugins.pylint.services

import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.Version
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.remote.RemoteSdkProperties
import com.jetbrains.python.sdk.pythonSdk
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.TestOnly
import works.szabope.plugins.common.services.BasicSettingsData
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.common.services.SettingsValidationProblem
import works.szabope.plugins.pylint.PylintArgs
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.common.dialog.IDialogManager
import works.szabope.plugins.pylint.services.cli.Cli
import works.szabope.plugins.pylint.services.cli.PythonEnvironmentAwareCli
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import java.io.File
import javax.swing.event.HyperlinkEvent

@State(name = "PylintSettings", storages = [Storage("PylintPlugin.xml")], category = SettingsCategory.PLUGINS)
class PylintSettings(internal val project: Project) :
    SimplePersistentStateComponent<PylintSettings.PylintState>(PylintState()), Settings {

    @ApiStatus.Internal
    class PylintState : BaseState() {
        var executablePath by string()
        var useProjectSdk by property(false)
        var configFilePath: String? by string()
        var arguments by string()
        var autoScrollToSource by property(false)
        var excludeNonProjectFiles by property(true)
        var projectDirectory by string()
        var scanBeforeCheckIn by property(false)
    }

    override var useProjectSdk
        get() = state.useProjectSdk
        set(value) {
            if (!value || validateSdk() == null) {
                state.useProjectSdk = value
            }
        }

    override var executablePath
        get() = state.executablePath
        set(value) {
            val validityProblem = validateExecutable(value)
            if (validityProblem == null) {
                state.executablePath = value
            } else {
                logger.warn("executablePath validation failed with '$validityProblem' for '$value'")
            }
        }

    override var configFilePath
        get() = state.configFilePath
        set(value) {
            val validityProblem = validateConfigFile(value)
            if (validityProblem == null) {
                state.configFilePath = value
            } else {
                logger.warn("configFilePath validation failed with '$validityProblem' for '$value")
            }
        }

    override var arguments
        get() = state.arguments
        set(value) {
            state.arguments = value
        }

    override var isAutoScrollToSource
        get() = state.autoScrollToSource
        set(value) {
            state.autoScrollToSource = value
        }

    override var excludeNonProjectFiles
        get() = state.excludeNonProjectFiles
        set(value) {
            state.excludeNonProjectFiles = value
        }

    override var projectDirectory
        get() = state.projectDirectory
        set(value) {
            val validityProblem = validateProjectDirectory(value)
            if (validityProblem == null) {
                state.projectDirectory = value
            } else {
                logger.warn("projectDirectory validation failed for '$validityProblem' with '$value'")
            }
        }

    override var scanBeforeCheckIn
        get() = state.scanBeforeCheckIn
        set(value) {
            state.scanBeforeCheckIn = value
        }

    // TODO: projectDirectory != null is failing for tests: cannot set /src because it does not exist
    override fun isComplete(): Boolean {
        return canExecute() && validateConfigFile(executablePath) == null && validateProjectDirectory(projectDirectory) == null
    }

    override fun getData() = PylintExecutorConfiguration(
        executablePath,
        useProjectSdk,
        configFilePath,
        arguments,
        projectDirectory!!,
        excludeNonProjectFiles,
        scanBeforeCheckIn
    )

    private fun canExecute(): Boolean {
        return if (useProjectSdk) {
            validateSdk() == null
        } else {
            executablePath != null && validateExecutable(executablePath) == null
        }
    }

    override fun ensureValid(): SettingsValidationProblem? {
        validateExecutable(executablePath)?.also {
            logger.warn("clearing invalid executablePath $executablePath")
            executablePath = null
            return@ensureValid it
        }
        validateConfigFile(configFilePath)?.also {
            logger.warn("clearing invalid configFilePath $configFilePath")
            configFilePath = null
            return@ensureValid it
        }
        validateProjectDirectory(projectDirectory)?.also {
            logger.warn("clearing invalid projectDirectory $projectDirectory")
            projectDirectory = null
            return@ensureValid it
        }
        return null
    }

    override suspend fun initSettings(oldSettings: BasicSettingsData?) {
        if (executablePath == null && oldSettings?.executablePath != null) {
            executablePath = oldSettings.executablePath
        }
        useProjectSdk = useProjectSdk || (executablePath == null && project.pythonSdk != null)
        if (!useProjectSdk && executablePath == null) {
            executablePath = autodetectExecutable()
        }
        if (configFilePath == null) {
            configFilePath = oldSettings?.configFilePath
        }
        if (arguments == null) {
            arguments = oldSettings?.arguments ?: PylintArgs.PYLINT_RECOMMENDED_COMMAND_ARGS
        }
        if (!scanBeforeCheckIn) {
            scanBeforeCheckIn = oldSettings?.scanBeforeCheckIn ?: false
        }
        if (projectDirectory == null) {
            projectDirectory = project.guessProjectDir()?.path
        }
    }

    override fun validateExecutable(path: String?): SettingsValidationProblem? {
        if (path == null) return null
        require(path.isNotBlank())
        val file = File(path)
        if (!file.exists()) {
            return SettingsValidationProblem(PylintBundle.message("pylint.settings.path_to_executable.not_exists"))
        }
        if (file.isDirectory) {
            return SettingsValidationProblem(PylintBundle.message("pylint.settings.path_to_executable.is_directory"))
        }
        if (!file.canExecute()) {
            return SettingsValidationProblem(PylintBundle.message("pylint.settings.path_to_executable.not_executable"))
        }

        val processResult = runBlocking {
            Cli.execute(path, "--version")
        }
        if (processResult.resultCode != 0) {
            return SettingsValidationProblem(
                PylintBundle.message(
                    "pylint.settings.path_to_executable.exited_with_error",
                    path,
                    processResult.resultCode,
                    processResult.stderr
                )
            )
        }

        val pylintVersion = "pylint (\\d+.\\d+.\\d+)".toRegex().find(processResult.stdout)?.groups?.last()?.value
        if (pylintVersion == null) {
            return SettingsValidationProblem(PylintBundle.message("pylint.settings.path_to_executable.unknown_version"))
        }
        return validateVersion(Version.parseVersion(pylintVersion)!!)
    }

    override fun validateSdk(): SettingsValidationProblem? {
        if ((project.pythonSdk?.sdkAdditionalData as? RemoteSdkProperties)?.sdkId?.startsWith("WSL") == true) {
            return SettingsValidationProblem(PylintBundle.message("pylint.settings.wsl_not_supported"))
        }
        val installedPackage =
            PylintPackageManagementFacade(project).getInstalledVersion() ?: return SettingsValidationProblem(
                PylintBundle.message("pylint.settings.pylint_not_installed")
            )
        return validateVersion(installedPackage)
    }

    private fun validateVersion(version: Version): SettingsValidationProblem? {
        if (!PylintPackageManagementFacade(project).isVersionSupported(version)) {
            return SettingsValidationProblem(
                PylintBundle.message(
                    "pylint.settings.pylint_invalid_version", "${version.major}.${version.minor}.${version.bugfix}"
                )
            )
        }
        return null
    }

    override fun validateConfigFile(path: String?): SettingsValidationProblem? {
        if (path != null) {
            require(path.isNotBlank())
            val file = File(path)
            if (!file.exists()) {
                return SettingsValidationProblem(PylintBundle.message("pylint.settings.path_to_config_file.not_exists"))
            }
            if (file.isDirectory) {
                return SettingsValidationProblem(PylintBundle.message("pylint.settings.path_to_config_file.is_directory"))
            }
        }
        return null
    }

    override fun validateProjectDirectory(path: String?): SettingsValidationProblem? {
        if (path != null) {
            require(path.isNotBlank())
            val file = File(path)
            if (!file.exists()) {
                return SettingsValidationProblem(PylintBundle.message("pylint.settings.path_to_project_directory.not_exist"))
            }
            if (!file.isDirectory) {
                return SettingsValidationProblem(PylintBundle.message("pylint.settings.path_to_project_directory.is_not_directory"))
            }
        }
        return null
    }

    override suspend fun autodetectExecutable(): String? {
        val locateCommand = if (SystemInfo.isWindows) arrayOf("where.exe", "pylint.exe") else arrayOf("which", "pylint")
        val processResult = PythonEnvironmentAwareCli(project).execute(command = locateCommand)
        return when (processResult.resultCode) { // same for linux and windows
            0 -> processResult.stdout.lines().first().trim().ifBlank { null }
            1 -> null
            else -> {
                ToolWindowManager.getInstance(project).notifyByBalloon(
                    PylintToolWindowPanel.ID,
                    MessageType.ERROR,
                    PylintBundle.message("pylint.toolwindow.balloon.external_error"),
                    null
                ) {
                    if (it.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                        IDialogManager.showToolExecutionErrorDialog(
                            locateCommand.joinToString(" "), processResult.stderr, processResult.resultCode
                        )
                    }
                }
                null
            }
        }
    }

    @TestOnly
    override fun reset() {
        loadState(PylintState())
    }

    companion object {
        val logger = thisLogger()
    }
}
