package works.szabope.plugins.pylint.toolWindow

import com.intellij.icons.AllIcons
import works.szabope.plugins.pylint.PylintBundle
import javax.swing.Icon

data class SeverityConfig(val level: String, val text: String, val description: String, val icon: Icon) {
    companion object {
        fun find(severity: String) = ALL.find { it.level == severity }

        @JvmStatic
        val ALL = arrayOf(
            SeverityConfig(
                "fatal",
                PylintBundle.message("action.PyLintDisplayFatalAction.text"),
                PylintBundle.message("action.PyLintDisplayFatalAction.description"),
                AllIcons.Status.FailedInProgress
            ),
            SeverityConfig(
                "error",
                PylintBundle.message("action.PyLintDisplayErrorsAction.text"),
                PylintBundle.message("action.PyLintDisplayErrorsAction.description"),
                AllIcons.General.Error
            ),
            SeverityConfig(
                "warning",
                PylintBundle.message("action.PyLintDisplayWarningsAction.text"),
                PylintBundle.message("action.PyLintDisplayWarningsAction.description"),
                AllIcons.General.Warning
            ),
            SeverityConfig(
                "convention",
                PylintBundle.message("action.PyLintDisplayConventionAction.text"),
                PylintBundle.message("action.PyLintDisplayConventionAction.description"),
                AllIcons.Nodes.Class
            ), SeverityConfig(
                "refactor",
                PylintBundle.message("action.PyLintDisplayRefactorAction.text"),
                PylintBundle.message("action.PyLintDisplayRefactorAction.description"),
                AllIcons.Actions.ForceRefresh
            ),
            SeverityConfig(
                "info",
                PylintBundle.message("action.PyLintDisplayInfoAction.text"),
                PylintBundle.message("action.PyLintDisplayInfoAction.description"),
                AllIcons.General.Information
            )
        )
    }
}
