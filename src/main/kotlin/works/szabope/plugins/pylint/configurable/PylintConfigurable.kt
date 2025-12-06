package works.szabope.plugins.pylint.configurable

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import works.szabope.plugins.common.configurable.ConfigurableConfiguration
import works.szabope.plugins.common.configurable.GeneralConfigurable
import works.szabope.plugins.common.trimToNull
import works.szabope.plugins.common.validator.FileValidator
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.action.InstallPylintAction
import works.szabope.plugins.pylint.services.PylintPluginPackageManagementService
import works.szabope.plugins.pylint.services.PylintSettings

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
        PylintBundle.message("pylint.settings.version_check"),
        PylintBundle.message("pylint.settings.use_project_sdk"),
        PylintBundle.message("pylint.settings.config_file.comment"),
        PylintBundle.message("pylint.configuration.arguments_description")
    )
) {

    override val settings get() = PylintSettings.getInstance(project)
    override val packageManager get() = PylintPluginPackageManagementService.getInstance(project)

    override fun validateExecutable(path: String?) = with(PylintValidator(project)) {
        path?.trimToNull()?.let { path ->
            validateExecutablePath(path) ?: validateVersion(path)
        }
    }

    override fun validateLocalSdk() = PylintValidator(project).validateProjectSdk()

    override fun validateConfigFilePath(
        builder: ValidationInfoBuilder, field: TextFieldWithBrowseButton
    ) = FileValidator().validateConfigFilePath(field.text.trimToNull())?.let { builder.error(it) }

    companion object {
        const val ID = "Settings.Pylint"
    }
}