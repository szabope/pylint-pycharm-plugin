package works.szabope.plugins.pylint.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.TestOnly
import works.szabope.plugins.common.services.BasicSettingsData

@Service(Service.Level.PROJECT)
@State(
    name = "PylintConfigService",
    storages = [Storage("pylint.xml", deprecated = true)],
    category = SettingsCategory.PLUGINS,
    allowLoadInTests = true,
)
class OldPylintSettings : SimplePersistentStateComponent<OldPylintSettings.OldPylintSettingsState>(
    OldPylintSettingsState()
), BasicSettingsData {

    @ApiStatus.Internal
    class OldPylintSettingsState : BaseState() {
        var customPylintPath by string()
        var pylintrcPath by string()
        var pylintArguments by string()
        var scanBeforeCheckin by property(false)
    }

    override val executablePath get() = state.customPylintPath
    override val configFilePath get() = state.pylintrcPath
    override val arguments get() = state.pylintArguments
    override val scanBeforeCheckIn get() = state.scanBeforeCheckin

    @TestOnly
    fun reset() {
        loadState(OldPylintSettingsState())
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): OldPylintSettings = project.service()
    }
}