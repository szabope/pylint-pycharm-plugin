package works.szabope.plugins.pylint.services

import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.text.SemVer
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus
import works.szabope.plugins.pylint.PylintArgs
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.dialog.IDialogManager
import works.szabope.plugins.pylint.services.cli.Cli
import works.szabope.plugins.pylint.services.cli.PythonEnvironmentAwareCli
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import java.io.File
import javax.swing.event.HyperlinkEvent

@Service(Service.Level.PROJECT)
@State(name = "PylintSettings", storages = [Storage("PylintPlugin.xml")], category = SettingsCategory.PLUGINS)
class PylintSettings(internal val project: Project) :
    SimplePersistentStateComponent<PylintSettings.PylintState>(PylintState()) {

    @ApiStatus.Internal
    class PylintState : BaseState() {
        var executablePath by string()
        var configFilePath by string()
        var arguments by string()
        var autoScrollToSource by property(false)
        var excludeNonProjectFiles by property(true)
        var projectDirectory by string()
        val customExclusions by list<String>()
        var scanBeforeCheckIn by property(false)
    }

    var executablePath
        get() = state.executablePath
        set(value) {
            val validityProblem = validateExecutable(value)
            if (validityProblem == null) {
                state.executablePath = value
            } else {
                logger.warn("executablePath validation failed with '$validityProblem' for '$value'")
            }
        }

    var configFilePath
        get() = state.configFilePath
        set(value) {
            val validityProblem = validateConfigFile(value)
            if (validityProblem == null) {
                state.configFilePath = value
            } else {
                logger.warn("configFilePath validation failed with '$validityProblem' for '$value")
            }
        }

    var arguments
        get() = state.arguments
        set(value) {
            state.arguments = value
        }

    var isAutoScrollToSource
        get() = state.autoScrollToSource
        set(value) {
            state.autoScrollToSource = value
        }

    var isExcludeNonProjectFiles
        get() = state.excludeNonProjectFiles
        set(value) {
            state.excludeNonProjectFiles = value
        }

    var projectDirectory
        get() = state.projectDirectory
        set(value) {
            val validityProblem = validateProjectDirectory(value)
            if (validityProblem == null) {
                state.projectDirectory = value
            } else {
                logger.warn("projectDirectory validation failed for '$validityProblem' with '$value'")
            }
        }

    val customExclusions
        get() = state.customExclusions

    fun addExclusion(exclusion: String) {
        require(exclusion.isNotBlank())
        if (!state.customExclusions.contains(exclusion)) {
            state.customExclusions.add(exclusion)
        }
    }

    fun removeExclusion(exclusion: String) {
        if (state.customExclusions.contains(exclusion)) {
            state.customExclusions.remove(exclusion)
        }
    }

    var isScanBeforeCheckIn
        get() = state.scanBeforeCheckIn
        set(value) {
            state.scanBeforeCheckIn = value
        }

    @JvmInline
    value class SettingsValidationProblem(val message: String) {
        override fun toString() = message
    }

    fun isComplete(): Boolean = state.executablePath != null && projectDirectory != null

    fun ensureValid(): SettingsValidationProblem? {
        validateExecutable(state.executablePath)?.also {
            logger.warn("clearing invalid executablePath $executablePath")
            executablePath = null
            return@ensureValid it
        }
        validateConfigFile(state.configFilePath)?.also {
            logger.warn("clearing invalid configFilePath $configFilePath")
            configFilePath = null
            return@ensureValid it
        }
        validateProjectDirectory(state.projectDirectory)?.also {
            logger.warn("clearing invalid projectDirectory $projectDirectory")
            projectDirectory = null
            return@ensureValid it
        }
        return null
    }

    suspend fun initSettings(oldPylintSettings: OldPylintSettings?) {
        if (executablePath == null) {
            executablePath = oldPylintSettings?.executablePath ?: autodetectExecutable()
        }
        if (configFilePath == null) {
            configFilePath = oldPylintSettings?.configFilePath
        }
        if (arguments == null) {
            arguments = oldPylintSettings?.arguments ?: PylintArgs.PYLINT_RECOMMENDED_COMMAND_ARGS
        }
        if (!isScanBeforeCheckIn) {
            isScanBeforeCheckIn = oldPylintSettings?.isScanBeforeCheckIn ?: false
        }
        if (projectDirectory == null) {
            projectDirectory = project.guessProjectDir()?.path
        }
    }

    fun validateExecutable(path: String?): SettingsValidationProblem? {
        if (path != null) {
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
                Cli.execute("$path --version")
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

            val minimumPylintVersionText = PylintBundle.message("pylint.minimumVersion")
            val minimumPylintVersion = SemVer.parseFromText(minimumPylintVersionText)!!
            val pylintVersion =
                "(\\d+.\\d+.\\d+)".toRegex().find(processResult.stdout)?.let { SemVer.parseFromText(it.value) }
            if (pylintVersion == null) {
                return SettingsValidationProblem(PylintBundle.message("pylint.settings.path_to_executable.unknown_version"))
            }
            if (!pylintVersion.isGreaterOrEqualThan(minimumPylintVersion)) {
                return SettingsValidationProblem(
                    PylintBundle.message(
                        "pylint.settings.pylint_invalid_version", processResult.stdout, minimumPylintVersionText
                    )
                )
            }
        }
        return null
    }

    fun validateConfigFile(path: String?): SettingsValidationProblem? {
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

    fun validateProjectDirectory(path: String?): SettingsValidationProblem? {
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

    suspend fun autodetectExecutable(): String? {
        val locateCommand = if (SystemInfo.isWindows) "where.exe pylint.exe" else "which pylint"
        val processResult = PythonEnvironmentAwareCli(project).execute(locateCommand)
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
                        IDialogManager.showPylintExecutionErrorDialog(
                            locateCommand, processResult.stderr, processResult.resultCode
                        )
                    }
                }
                null
            }
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PylintSettings = project.service()

        val logger = thisLogger()
    }
}
