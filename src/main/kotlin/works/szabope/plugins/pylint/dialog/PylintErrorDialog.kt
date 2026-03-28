package works.szabope.plugins.pylint.dialog

import works.szabope.plugins.common.dialog.PluginErrorDescription
import works.szabope.plugins.common.dialog.PluginErrorDialog
import works.szabope.plugins.pylint.PylintBundle

class PylintPackageInstallationErrorDialog(message: String) : PluginErrorDialog(
    PylintBundle.message("pylint.dialog.installation_error.title"),
    PluginErrorDescription(message, PylintBundle.message("pylint.dialog.installation_error.message"))
)

class PylintExecutionErrorDialog(
    commandLine: String, result: String, resultCode: Int?
) : PluginErrorDialog(
    PylintBundle.message("pylint.dialog.execution_error.title"), PluginErrorDescription(
        PylintBundle.message("pylint.dialog.execution_error.content", commandLine, result),
        resultCode?.let { PylintBundle.message("pylint.dialog.execution_error.status_code", it) })
)

class PylintParseErrorDialog(
    commandLine: String, targets: String, json: String, error: String
) : PluginErrorDialog(
    PylintBundle.message("pylint.dialog.parse_error.title"), PluginErrorDescription(
        PylintBundle.message("pylint.dialog.parse_error.details", commandLine, targets, json),
        error.ifEmpty { null }?.let { PylintBundle.message("pylint.dialog.parse_error.message", it) })
)

class PylintGeneralErrorDialog(throwable: Throwable) : PluginErrorDialog(
    PylintBundle.message("pylint.dialog.general_error.title"), PluginErrorDescription(
        PylintBundle.message(
            "pylint.dialog.general_error.details", throwable.message ?: throwable.toString(), throwable.stackTraceToString()
        ), PylintBundle.message("pylint.please_report_this_issue")
    )
)
