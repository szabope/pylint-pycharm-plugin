package works.szabope.plugins.pylint.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.NlsContexts.DetailedDescription
import com.intellij.openapi.util.NlsContexts.DialogTitle
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import works.szabope.plugins.pylint.PylintBundle

data class PylintErrorDescription(
    @DetailedDescription val details: String, @DetailedDescription val message: String? = null
)

class PylintExecutionErrorDialog(command: String, result: String, resultCode: Int) : PylintErrorDialog(
    PylintBundle.message("pylint.dialog.execution_error.title"),
    PylintErrorDescription(
        PylintBundle.message("pylint.dialog.execution_error.content", command, result),
        PylintBundle.message("pylint.dialog.execution_error.status_code", resultCode)
    )
)

class PylintParseErrorDialog(command: String, commandOutput: String, error: String) : PylintErrorDialog(
    PylintBundle.message("pylint.dialog.parse_error.title"),
    PylintErrorDescription(
        PylintBundle.message("pylint.dialog.parse_error.details", command, commandOutput),
        PylintBundle.message("pylint.dialog.parse_error.message", error)
    )
)

open class PylintErrorDialog(
    title: @DialogTitle String, private val description: PylintErrorDescription
) : DialogWrapper(false) {

    init {
        super.init()
        super.setTitle(title)
        super.setErrorText(description.message)
    }

    override fun createCenterPanel() = panel {
        row {
            textArea().applyToComponent {
                text = description.details
                isEditable = false
                columns = COLUMNS_LARGE
                lineWrap = true
            }.align(Align.FILL)
        }.layout(RowLayout.PARENT_GRID)
    }
}
