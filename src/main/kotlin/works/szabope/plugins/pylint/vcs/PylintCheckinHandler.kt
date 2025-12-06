package works.szabope.plugins.pylint.vcs

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.checkin.*
import com.intellij.openapi.vcs.ui.RefreshableOnComponent
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.platform.util.progress.withProgressText
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.services.ScanService
import works.szabope.plugins.pylint.services.parser.PylintMessageConverter
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import works.szabope.plugins.pylint.toolWindow.PylintTreeService
import javax.swing.JCheckBox
import javax.swing.JComponent

class PylintCheckinHandler(panel: CheckinProjectPanel) : CheckinHandler(), CommitCheck {

    private val settings = PylintSettings.getInstance(panel.project)
    private val scanService = ScanService.getInstance(panel.project)
    private val treeService = PylintTreeService.getInstance(panel.project)

    override fun getBeforeCheckinConfigurationPanel(): RefreshableOnComponent {
        return object : RefreshableOnComponent {

            val checkbox = JCheckBox(PylintBundle.message("pylint.checkin-handler.checkbox"))

            override fun saveState() {
                settings.scanBeforeCheckIn = checkbox.isSelected
            }

            override fun restoreState() {
                checkbox.isSelected = settings.scanBeforeCheckIn
            }

            override fun getComponent(): JComponent {
                return panel {
                    row {
                        cell(checkbox)
                    }
                }
            }
        }
    }

    override fun getExecutionOrder() = CommitCheck.ExecutionOrder.EARLY

    override fun isEnabled() = settings.scanBeforeCheckIn && settings.getValidConfiguration().isSuccess

    override suspend fun runCheck(commitInfo: CommitInfo): CommitProblem? {
        val changes = commitInfo.committedChanges
        if (changes.isEmpty()) return null
        val files = changes.mapNotNull { it.afterRevision?.file?.virtualFile }
        val configuration = settings.getValidConfiguration().getOrThrow()
        val scanResults = withProgressText(PylintBundle.message("pylint.checkin-handler.in-progress")) {
            withContext(Dispatchers.Default) {
                scanService.scan(files, configuration)
            }
        }
        if (scanResults.isEmpty()) return null
        return object : CommitProblemWithDetails {
            override val text: String
                get() = PylintBundle.message("dialog.pre-checkin-confirmation.text", scanResults.size)
            override val showDetailsAction: String
                get() = PylintBundle.message("dialog.pre-checkin-confirmation.review")

            override fun showDetails(project: Project) {
                treeService.reinitialize(files)
                scanResults.map { PylintMessageConverter.convert(it) }.forEach {
                    treeService.add(it)
                }
                ToolWindowManager.getInstance(project).getToolWindow(PylintToolWindowPanel.ID)?.show()
            }
        }
    }
}