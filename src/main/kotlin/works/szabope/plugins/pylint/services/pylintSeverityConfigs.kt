package works.szabope.plugins.pylint.services

import com.intellij.icons.AllIcons
import works.szabope.plugins.common.services.SeverityConfig
import works.szabope.plugins.pylint.PylintBundle

val pylintSeverityConfigs = mapOf(
    "fatal" to SeverityConfig(
        "fatal",
        PylintBundle.message("action.PyLintDisplayFatalAction.text"),
        PylintBundle.message("action.PyLintDisplayFatalAction.description"),
        AllIcons.Status.FailedInProgress
    ),
    "error" to SeverityConfig(
        "error",
        PylintBundle.message("action.PyLintDisplayErrorsAction.text"),
        PylintBundle.message("action.PyLintDisplayErrorsAction.description"),
        AllIcons.General.Error
    ),
    "warning" to SeverityConfig(
        "warning",
        PylintBundle.message("action.PyLintDisplayWarningsAction.text"),
        PylintBundle.message("action.PyLintDisplayWarningsAction.description"),
        AllIcons.General.Warning
    ),
    "convention" to SeverityConfig(
        "convention",
        PylintBundle.message("action.PyLintDisplayConventionAction.text"),
        PylintBundle.message("action.PyLintDisplayConventionAction.description"),
        AllIcons.Nodes.Class
    ),
    "refactor" to SeverityConfig(
        "refactor",
        PylintBundle.message("action.PyLintDisplayRefactorAction.text"),
        PylintBundle.message("action.PyLintDisplayRefactorAction.description"),
        AllIcons.Actions.ForceRefresh
    ),
    "info" to SeverityConfig(
        "info",
        PylintBundle.message("action.PyLintDisplayInfoAction.text"),
        PylintBundle.message("action.PyLintDisplayInfoAction.description"),
        AllIcons.General.Information
    )
)