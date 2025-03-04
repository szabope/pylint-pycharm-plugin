package works.szabope.plugins.common.configurable

import com.intellij.grazie.utils.trimToNull
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Condition
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.layout.and
import com.jetbrains.python.sdk.PySdkPopupFactory
import com.jetbrains.python.sdk.noInterpreterMarker
import com.jetbrains.python.sdk.pythonSdk
import org.jetbrains.annotations.ApiStatus
import works.szabope.plugins.common.CommonBundle
import works.szabope.plugins.common.services.IPackageManagementFacade
import works.szabope.plugins.common.services.Settings
import javax.swing.JButton

data class ConfigurableConfiguration(
    val displayName: String,
    val helpTopic: String,
    val id: String,
    val installActionId: String,
    val installButtonText: String,
    val pickerTitle: String,
    val pickerDirectOptionTitle: String,
    val pickerDirectOptionFileFilter: GeneralConfigurable.FileFilter,
    val pickerDirectOptionEmptyWarning: String,
    val pickerSdkOptionTitle: String,
    val configFilePickerRowComment: String,
    val recommendedArguments: String,
)

abstract class GeneralConfigurable<T : BaseState>(
    private val project: Project, private val config: ConfigurableConfiguration
) : BoundSearchableConfigurable(config.displayName, config.helpTopic, config.id), Configurable.NoScroll {

    protected abstract val settings: Settings<T>
    protected abstract val packageManagementService: IPackageManagementFacade

    override fun createPanel(): DialogPanel {
        val pnl = panel {
            indent {
                toolPicker()
                configFilePicker()
                argumentsField()
                projectDirectoryPicker()
                excludeNonProjectFilesCheckbox()
            }
        }
        pnl.registerValidators(disposable!!)
        pnl.validateAll()
        return pnl
    }


    override fun apply() {
        if ((createComponent() as DialogPanel).validateAll().isEmpty()) {
            super.apply()
        }
    }

    @ApiStatus.Internal
    class FileFilter(private val fileNames: List<String>) : Condition<VirtualFile> {
        override fun value(t: VirtualFile?): Boolean {
            return fileNames.contains(t?.name ?: return false)
        }
    }

    private fun Row.installButton(enabled: ComponentPredicate) {
        val buttonClicked = AtomicBooleanProperty(false)
        val action = ActionManager.getInstance().getAction(config.installActionId)
        label(project.pythonSdk?.let { PySdkPopupFactory.shortenNameInPopup(it, 50) } ?: noInterpreterMarker).align(
            Align.FILL
        )
        lateinit var result: Cell<JButton>
        result = button(config.installButtonText) {
            val dataContext = DataManager.getInstance().getDataContext(result.component)
            val event = AnActionEvent.createEvent(
                action, dataContext, null, ActionPlaces.UNKNOWN, ActionUiKind.NONE, null
            )
            ActionUtil.invokeAction(action, event) {
                buttonClicked.set(false)
            }
        }.enabledIf(object : ComponentPredicate() {
            override fun invoke() = !buttonClicked.get() && packageManagementService.canInstall()
            override fun addListener(listener: (Boolean) -> Unit) {
                buttonClicked.afterChange(listener)
            }
        }.and(enabled))
    }

    private fun Panel.toolPicker() = buttonsGroup(title = config.pickerTitle) {
        row {
            val executableOption = radioButton(config.pickerDirectOptionTitle, !USE_PROJECT_SDK)
            executableOption.component
            val executableChooserDescriptor =
                FileChooserDescriptor(true, false, false, false, false, false).withFileFilter(
                    config.pickerDirectOptionFileFilter
                )
            val pathToExecutableField = textFieldWithBrowseButton(
                project = project, fileChooserDescriptor = executableChooserDescriptor
            )
            val pathToExecutableComponent = pathToExecutableField.component
            pathToExecutableField.align(Align.FILL).bindText(
                getter = { settings.executablePath.orEmpty() },
                setter = { settings.executablePath = it.trimToNull() },
            ).validationOnInput { field ->
                if (field.text.isBlank()) {
                    return@validationOnInput warning(config.pickerDirectOptionEmptyWarning)
                }
                null
            }.validationOnApply {
                settings.validateExecutable(pathToExecutableComponent.text.trimToNull())?.also {
                    return@validationOnApply error(it.message)
                }
                null
            }.resizableColumn().enabledIf(executableOption.selected)
        }.layout(RowLayout.PARENT_GRID)
        row {
            val sdkOption = radioButton(config.pickerSdkOptionTitle, USE_PROJECT_SDK).enabled(
                project.pythonSdk != null
            ).validationOnInput {
                settings.validateSdk()?.also {
                    return@validationOnInput error(it.message)
                }
                null
            }
            sdkOption.component
            installButton(sdkOption.selected)
        }.rowComment(
            comment = if (!packageManagementService.isLocalEnvironment()) {
                CommonBundle.message("settings.system_wide_installation_warning")
            } else {
                ""
            }, maxLineLength = MAX_LINE_LENGTH_WORD_WRAP
        ).layout(RowLayout.PARENT_GRID)
    }.bind(getter = { settings.useProjectSdk }, setter = { settings.useProjectSdk = it })

    private fun Panel.configFilePicker() = row {
        label(CommonBundle.message("settings.config_file.label"))
        textFieldWithBrowseButton(project = project).align(Align.FILL).bindText(
            getter = { settings.configFilePath.orEmpty() },
            setter = { settings.configFilePath = it.trimToNull() },
        ).validationOnApply { field ->
            settings.validateConfigFile(field.text.trimToNull())?.also {
                return@validationOnApply error(it.message)
            }
            null
        }
    }.rowComment(
        config.configFilePickerRowComment, maxLineLength = MAX_LINE_LENGTH_WORD_WRAP
    ).layout(RowLayout.PARENT_GRID)

    private fun Panel.argumentsField() = row {
        label(CommonBundle.message("settings.arguments.label"))
        textField().align(Align.FILL).bindText(
            getter = { settings.arguments.orEmpty() },
            setter = { settings.arguments = it.trimToNull() },
        )
    }.rowComment(
        CommonBundle.message("settings.arguments.hint_recommended", config.recommendedArguments),
        maxLineLength = MAX_LINE_LENGTH_WORD_WRAP
    ).layout(RowLayout.PARENT_GRID)

    private fun Panel.projectDirectoryPicker() = row {
        label(CommonBundle.message("settings.project_directory.label"))
        val directoryChooserDescriptor = FileChooserDescriptor(false, true, false, false, false, false)
        val projectDirectoryField = textFieldWithBrowseButton(
            project = project, fileChooserDescriptor = directoryChooserDescriptor
        )
        projectDirectoryField.align(Align.FILL).bindText(
            getter = { settings.projectDirectory.orEmpty() },
            setter = { settings.projectDirectory = it.trimToNull() },
        ).validationOnInput { field ->
            if (field.text.isBlank()) {
                return@validationOnInput warning(CommonBundle.message("settings.project_directory.empty_warning"))
            }
            null
        }.validationOnApply {
            settings.validateProjectDirectory(projectDirectoryField.component.text.trimToNull())?.also {
                return@validationOnApply error(it.message)
            }
            null
        }
    }.layout(RowLayout.PARENT_GRID)

    private fun Panel.excludeNonProjectFilesCheckbox() = row {
        checkBox(CommonBundle.message("settings.exclude_non_project_files.label")).bindSelected(
            getter = { settings.isExcludeNonProjectFiles },
            setter = { settings.isExcludeNonProjectFiles = it })
    }.layout(RowLayout.PARENT_GRID)

    companion object {
        const val USE_PROJECT_SDK = true
    }
}