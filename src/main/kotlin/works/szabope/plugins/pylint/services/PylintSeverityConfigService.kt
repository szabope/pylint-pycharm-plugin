package works.szabope.plugins.pylint.services

import com.intellij.icons.AllIcons
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.service.SeverityConfig
import works.szabope.plugins.common.service.SeverityConfigService
import works.szabope.plugins.pylint.PylintBundle

@Service(Service.Level.PROJECT)
class PylintSeverityConfigService : SeverityConfigService {
    override fun getAll() = ALL

    companion object {

        @JvmStatic
        fun getInstance(project: Project): PylintSeverityConfigService = project.service()

        @JvmStatic
        val ALL = setOf(
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