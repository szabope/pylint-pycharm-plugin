package works.szabope.plugins.pylint.configurable

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import works.szabope.plugins.common.configurable.ConfigurableConfiguration
import works.szabope.plugins.common.configurable.GeneralConfigurable
import works.szabope.plugins.common.services.IPackageManagementFacade
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.pylint.PylintArgs
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.action.InstallPylintAction
import works.szabope.plugins.pylint.services.PylintPackageManagementFacade

class PylintConfigurable(private val project: Project) : GeneralConfigurable(
    project, ConfigurableConfiguration(
        PylintBundle.message("pylint.configurable.name"),
        PylintBundle.message("pylint.configurable.name"),
        ID,
        InstallPylintAction.ID,
        PylintBundle.message("pylint.intention.install_pylint.text"),
        PylintBundle.message("pylint.settings.pylint_picker_title"),
        PylintBundle.message("pylint.settings.path_to_executable.label"),
        FileFilter(
            if (SystemInfo.isWindows) {
                listOf("pylint.exe", "pylint.bat")
            } else {
                listOf("pylint")
            }
        ),
        PylintBundle.message("pylint.settings.path_to_executable.empty_warning"),
        PylintBundle.message("pylint.settings.use_project_sdk"),
        PylintBundle.message("pylint.settings.config_file.comment"),
        PylintArgs.PYLINT_RECOMMENDED_COMMAND_ARGS
    )
) {

    override val settings: Settings
        get() = Settings.getInstance(project)

    override val packageManagementService: IPackageManagementFacade
        get() = PylintPackageManagementFacade(project)

    companion object {
        const val ID = "Settings.Pylint"
    }
}