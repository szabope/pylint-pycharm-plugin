package works.szabope.plugins.pylint.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.jetbrains.python.sdk.pythonSdk
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.TestOnly
import works.szabope.plugins.common.services.BasicSettingsData
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.Settings

@Service(Service.Level.PROJECT)
@State(name = "PylintSettings", storages = [Storage("PylintPlugin.xml")], category = SettingsCategory.PLUGINS)
class PylintSettings(internal val project: Project) :
    SimplePersistentStateComponent<PylintSettings.PylintState>(PylintState()), Settings {

    private var initialized = false

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
            state.useProjectSdk = value
        }

    override var executablePath
        get() = state.executablePath?.trim() ?: ""
        set(value) {
            // workaround for string() normalizes empty string to null
            state.executablePath = value.ifBlank { " " }
        }

    override var configFilePath
        get() = state.configFilePath?.trim() ?: ""
        set(value) {
            // workaround for string() normalizes empty string to null
            state.configFilePath = value.ifBlank { " " }
        }

    override var arguments
        get() = state.arguments?.trim() ?: ""
        set(value) {
            // workaround for string() normalizes empty string to null
            state.arguments = value.ifBlank { " " }
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

    override var workingDirectory
        get() = state.projectDirectory
        set(value) {
            state.projectDirectory = value
        }

    override var scanBeforeCheckIn
        get() = state.scanBeforeCheckIn
        set(value) {
            state.scanBeforeCheckIn = value
        }

    override suspend fun initSettings(oldSettings: BasicSettingsData) {
        if (state.executablePath == null && oldSettings.executablePath != null) {
            executablePath = oldSettings.executablePath!!
        }
        if (executablePath.isNotBlank() && project.pythonSdk == null) {
            useProjectSdk = false
        }
        if (state.configFilePath == null && oldSettings.configFilePath != null) {
            configFilePath = oldSettings.configFilePath!!
        }
        if (state.arguments == null && oldSettings.arguments != null) {
            arguments = oldSettings.arguments!!
        }
        if (state.projectDirectory == null) {
            workingDirectory = project.guessProjectDir()?.canonicalPath
        }
        initialized = true
    }

    override fun getValidConfiguration(): Result<ImmutableSettingsData> {
        val workingDirectory = workingDirectory
        if (workingDirectory.isNullOrBlank()) {
            return Result.failure(PylintSettingsInvalid("Working directory is required"))
        }
        if (!isToolSet()) {
            return Result.failure(PylintSettingsInvalid("Pylint tool is not set"))
        }

        return PylintExecutorConfiguration(
            executablePath,
            useProjectSdk,
            configFilePath,
            arguments,
            workingDirectory,
            excludeNonProjectFiles,
            scanBeforeCheckIn
        ).let { Result.success(it) }
    }

    private fun isToolSet(): Boolean {
        return if (useProjectSdk) {
            project.pythonSdk != null && PylintPluginPackageManagementService.getInstance(project)
                .checkInstalledRequirement().isSuccess
        } else {
            executablePath.isNotBlank()
        }
    }


    @TestOnly
    fun reset() {
        loadState(PylintState())
    }

    fun isInitialized() = initialized

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PylintSettings = project.service()
    }
}
