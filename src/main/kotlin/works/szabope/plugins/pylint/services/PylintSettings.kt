package works.szabope.plugins.pylint.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus
import works.szabope.plugins.common.blankToSingleSpace
import works.szabope.plugins.common.services.AbstractToolSettings

@Service(Service.Level.PROJECT)
@State(name = "PylintSettings", storages = [Storage("PylintPlugin.xml")], category = SettingsCategory.PLUGINS)
class PylintSettings(private val project: Project) : AbstractToolSettings<PylintSettings.PylintState>(project, PylintState()) {

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
        set(value) { state.useProjectSdk = value }

    override var executablePath
        get() = state.executablePath?.trim() ?: ""
        set(value) { state.executablePath = value.blankToSingleSpace() }

    override var configFilePath
        get() = state.configFilePath?.trim() ?: ""
        set(value) { state.configFilePath = value.blankToSingleSpace() }

    override var arguments
        get() = state.arguments?.trim() ?: ""
        set(value) { state.arguments = value.blankToSingleSpace() }

    override var isAutoScrollToSource
        get() = state.autoScrollToSource
        set(value) { state.autoScrollToSource = value }

    override var excludeNonProjectFiles
        get() = state.excludeNonProjectFiles
        set(value) { state.excludeNonProjectFiles = value }

    override var workingDirectory
        get() = state.projectDirectory
        set(value) { state.projectDirectory = value }

    override var scanBeforeCheckIn
        get() = state.scanBeforeCheckIn
        set(value) { state.scanBeforeCheckIn = value }

    override fun getPackageManagementService() = PylintPluginPackageManagementService.getInstance(project)
    override fun toolNotSetMessage() = "Pylint tool is not set"
    override fun isExecutableStateNull() = state.executablePath == null
    override fun isConfigFileStateNull() = state.configFilePath == null
    override fun isArgumentsStateNull() = state.arguments == null
    override fun initialState() = PylintState()

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PylintSettings = project.service()
    }
}