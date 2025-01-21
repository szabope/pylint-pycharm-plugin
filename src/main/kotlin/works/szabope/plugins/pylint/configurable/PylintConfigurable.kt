package works.szabope.plugins.pylint.configurable

import com.intellij.grazie.utils.trimToNull
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.ui.components.textFieldWithBrowseButton
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ComponentPredicate
import org.jetbrains.annotations.ApiStatus
import works.szabope.plugins.pylint.PylintArgs
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.action.InstallPylintAction
import works.szabope.plugins.pylint.services.PylintPackageUtil
import works.szabope.plugins.pylint.services.PylintSettings
import javax.swing.JButton

class PylintConfigurable(private val project: Project) : BoundSearchableConfigurable(
    PylintBundle.message("pylint.configurable.name"), PylintBundle.message("pylint.configurable.name"), _id = ID
), Configurable.NoScroll {

    private val settings
        get() = PylintSettings.getInstance(project)

    private val executableChooserDescriptor =
        FileChooserDescriptor(true, false, false, false, false, false).withFileFilter(
            FileFilter(
                if (SystemInfo.isWindows) {
                    listOf("pylint.exe", "pylint.bat")
                } else {
                    listOf("pylint")
                }
            )
        )

    private val directoryChooserDescriptor = FileChooserDescriptor(false, true, false, false, false, false)

    override fun createPanel(): DialogPanel {
        val pathToExecutableComponent =
            textFieldWithBrowseButton(project = project, fileChooserDescriptor = executableChooserDescriptor)
        return panel {
            indent {
                row {
                    label(PylintBundle.message("pylint.settings.path_to_executable.label"))
                    val pathToExecutableField = cell(pathToExecutableComponent)
                    pathToExecutableField.align(Align.FILL).bindText(
                        getter = { settings.executablePath.orEmpty() },
                        setter = { settings.executablePath = it.trimToNull() },
                    ).validationOnInput { field ->
                        if (field.text.isBlank()) {
                            val message = PylintBundle.message("pylint.settings.path_to_executable.empty_warning")
                            return@validationOnInput warning(message)
                        }
                        null
                    }.validationOnApply {
                        settings.validateExecutable(pathToExecutableComponent.text.trimToNull())?.also {
                            return@validationOnApply error(it.message)
                        }
                        null
                    }.resizableColumn()
                    button(PylintBundle.message("pylint.settings.autodetect.label")) {
                        runWithModalProgressBlocking(
                            project, PylintBundle.message("pylint.settings.autodetect.in_progress")
                        ) {
                            pathToExecutableComponent.text = settings.autodetectExecutable() ?: ""
                        }
                    }.enabledIf(object : ComponentPredicate() {
                        override fun invoke() = pathToExecutableComponent.text.isBlank()

                        override fun addListener(listener: (Boolean) -> Unit) {
                            pathToExecutableField.onChanged { listener(it.text.isBlank()) }
                        }
                    }).align(AlignX.RIGHT + AlignY.CENTER)
                }.rowComment(
                    PylintBundle.message(
                        "pylint.settings.path_to_executable.comment", PylintArgs.PYLINT_MANDATORY_COMMAND_ARGS
                    ), maxLineLength = MAX_LINE_LENGTH_WORD_WRAP
                ).layout(RowLayout.PARENT_GRID)
                row {
                    label(PylintBundle.message("pylint.settings.config_file.label"))
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
                    PylintBundle.message("pylint.settings.config_file.comment"),
                    maxLineLength = MAX_LINE_LENGTH_WORD_WRAP
                ).layout(RowLayout.PARENT_GRID)
                row {
                    label(PylintBundle.message("pylint.settings.arguments.label"))
                    textField().align(Align.FILL).bindText(
                        getter = { settings.arguments.orEmpty() },
                        setter = { settings.arguments = it.trimToNull() },
                    )
                }.rowComment(
                    PylintBundle.message(
                        "pylint.settings.arguments.hint_recommended", PylintArgs.PYLINT_RECOMMENDED_COMMAND_ARGS
                    ), maxLineLength = MAX_LINE_LENGTH_WORD_WRAP
                ).layout(RowLayout.PARENT_GRID)
                row {
                    label(PylintBundle.message("pylint.settings.project_directory.label"))
                    val projectDirectoryField = textFieldWithBrowseButton(
                        project = project, fileChooserDescriptor = directoryChooserDescriptor
                    )
                    projectDirectoryField.align(Align.FILL).bindText(
                        getter = { settings.projectDirectory.orEmpty() },
                        setter = { settings.projectDirectory = it.trimToNull() },
                    ).validationOnInput { field ->
                        if (field.text.isBlank()) {
                            val message =
                                PylintBundle.message("pylint.settings.path_to_project_directory.empty_warning")
                            return@validationOnInput warning(message)
                        }
                        null
                    }.validationOnApply {
                        settings.validateProjectDirectory(projectDirectoryField.component.text.trimToNull())?.also {
                            return@validationOnApply error(it.message)
                        }
                        null
                    }
                }.layout(RowLayout.PARENT_GRID)
                row {
                    checkBox(PylintBundle.message("pylint.settings.exclude_non_project_files.label")).bindSelected(
                        getter = { settings.isExcludeNonProjectFiles },
                        setter = { settings.isExcludeNonProjectFiles = it })
                }.layout(RowLayout.PARENT_GRID)
                row {
                    val buttonClicked = AtomicBooleanProperty(false)
                    val action = ActionManager.getInstance().getAction(InstallPylintAction.ID)
                    lateinit var result: Cell<JButton>
                    result = button(PylintBundle.message("pylint.intention.install_pylint.text")) {
                        val dataContext = DataManager.getInstance().getDataContext(result.component)
                        val event = AnActionEvent.createEvent(
                            action, dataContext, null, ActionPlaces.UNKNOWN, ActionUiKind.NONE, null
                        )
                        ActionUtil.invokeAction(action, event) {
                            buttonClicked.set(false)
                            if (pathToExecutableComponent.text.isBlank()) {
                                runWithModalProgressBlocking(
                                    project, PylintBundle.message("pylint.settings.autodetect.in_progress")
                                ) {
                                    pathToExecutableComponent.text = settings.autodetectExecutable() ?: ""
                                }
                            }
                        }
                    }.enabledIf(object : ComponentPredicate() {
                        override fun invoke() = !buttonClicked.get() && PylintPackageUtil.canInstall(project)
                        override fun addListener(listener: (Boolean) -> Unit) = buttonClicked.afterChange(listener)
                    })
                    if (!PylintPackageUtil.isLocalEnvironment(project)) {
                        result.comment(PylintBundle.message("pylint.intention.install_pylint.not_supported.system_wide"))
                    }
                }.layout(RowLayout.PARENT_GRID)
            }
        }
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

    companion object {
        const val ID = "Settings.Pylint"
    }
}