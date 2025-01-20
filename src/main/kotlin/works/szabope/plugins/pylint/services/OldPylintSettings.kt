package works.szabope.plugins.pylint.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus

@Service(Service.Level.PROJECT)
@State(
    name = "PylintConfigService",
    storages = [Storage("pylint.xml", deprecated = true)],
    category = SettingsCategory.PLUGINS,
    allowLoadInTests = true,
)
class OldPylintSettings : SimplePersistentStateComponent<OldPylintSettings.OldPylintSettingsState>(
    OldPylintSettingsState()
) {

    @ApiStatus.Internal
    class OldPylintSettingsState : BaseState() {
        var customPylintPath by string()
        var pylintrcPath by string()
        var pylintArguments by string()
        var scanBeforeCheckin by property(false)
    }

    val executablePath
        get() = state.customPylintPath
    val configFilePath
        get() = state.pylintrcPath
    val arguments
        get() = state.pylintArguments
    val isScanBeforeCheckIn
        get() = state.scanBeforeCheckin

    companion object {
        @JvmStatic
        fun getInstance(project: Project): OldPylintSettings = project.service()
    }
}