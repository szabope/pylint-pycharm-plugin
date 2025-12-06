package works.szabope.plugins.pylint.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.NlsContexts.DetailedDescription
import com.intellij.openapi.util.NlsContexts.DialogTitle
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import com.jetbrains.rd.generator.nova.GenerationSpec.Companion.nullIfEmpty
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.pylint.PylintBundle
import java.awt.Dimension

data class PylintErrorDescription(
    @DetailedDescription val details: String?, @DetailedDescription val message: String? = null
)

class PylintPackageInstallationErrorDialog(message: String) : PylintErrorDialog(
    PylintBundle.message("pylint.dialog.installation_error.title"),
    PylintErrorDescription(message, PylintBundle.message("pylint.dialog.installation_error.message"))
)

class FailedToExecuteErrorDialog(message: String) : PylintErrorDialog(
    PylintBundle.message("pylint.dialog.failed_to_execute.title"), PylintErrorDescription(
        message, PylintBundle.message("pylint.dialog.failed_to_execute.message")
    )
)

class PylintExecutionErrorDialog(
    configuration: ImmutableSettingsData, result: String, resultCode: Int?
) : PylintErrorDialog(
    PylintBundle.message("pylint.dialog.execution_error.title"), PylintErrorDescription(
        PylintBundle.message("pylint.dialog.execution_error.content", configuration, result),
        resultCode?.let { PylintBundle.message("pylint.dialog.execution_error.status_code", it) })
)

class PylintParseErrorDialog(
    configuration: ImmutableSettingsData, targets: String, json: String, error: String
) : PylintErrorDialog(
    PylintBundle.message("pylint.dialog.parse_error.title"), PylintErrorDescription(
        PylintBundle.message("pylint.dialog.parse_error.details", configuration, targets, json),
        error.nullIfEmpty()?.let { PylintBundle.message("pylint.dialog.parse_error.message", it) })
)

class PylintGeneralErrorDialog(throwable: Throwable) : PylintErrorDialog(
    PylintBundle.message("pylint.dialog.general_error.title"), PylintErrorDescription(
        PylintBundle.message(
            "pylint.dialog.general_error.details", throwable.message!!, throwable.stackTraceToString()
        ), PylintBundle.message("pylint.please_report_this_issue")
    )
)

open class PylintErrorDialog(
    title: @DialogTitle String, private val description: PylintErrorDescription
) : DialogWrapper(false) {

    init {
        setTitle(title)
        super.init()
        setErrorText(description.message)
        contentPanel.maximumSize = Dimension(JBUI.scale(800), contentPanel.preferredSize.height)
    }

    override fun createCenterPanel() = description.details?.let { details ->
        panel {
            row {
                textArea().applyToComponent {
                    text = details
                    isEditable = false
                    lineWrap = true
                    wrapStyleWord = true
                }.also {
                    setSize(JBUI.scale(800), 0)
                }.align(AlignX.FILL)
            }
        }
    }
}
