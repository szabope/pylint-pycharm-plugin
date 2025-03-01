package works.szabope.plugins.pylint.vcs

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.checkin.*
import com.intellij.openapi.vcs.ui.RefreshableOnComponent
import com.intellij.platform.util.progress.withProgressText
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.common.messages.TreeListener
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.messages.PylintMessageConverter
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.services.ScanService
import works.szabope.plugins.pylint.toRunConfiguration
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import javax.swing.JCheckBox
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class PylintCheckinHandler(private val panel: CheckinProjectPanel) : CheckinHandler(), CommitCheck {

    private val settings = PylintSettings.getInstance(panel.project)
    private val service = ScanService.getInstance(panel.project)

    override fun getBeforeCheckinConfigurationPanel(): RefreshableOnComponent {
        return object : RefreshableOnComponent {

            val checkbox = JCheckBox(PylintBundle.message("pylint.checkin-handler.checkbox"))

            override fun saveState() {
                settings.isScanBeforeCheckIn = checkbox.isSelected
            }

            override fun restoreState() {
                checkbox.isSelected = settings.isScanBeforeCheckIn
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

    override fun isEnabled() = PylintSettings.getInstance(panel.project).isScanBeforeCheckIn

    override suspend fun runCheck(commitInfo: CommitInfo): CommitProblem? {
        val changes = commitInfo.committedChanges
        if (changes.isEmpty()) return null
        val files = changes.mapNotNull { it.afterRevision?.file?.virtualFile }
        val runConfiguration = PylintSettings.getInstance(panel.project).toRunConfiguration()
        val scanResults = withProgressText(PylintBundle.message("pylint.checkin-handler.in-progress")) {
            withContext(Dispatchers.Default) {
                service.scan(files, runConfiguration)
            }
        }
        if (scanResults.isEmpty()) return null
        return object : CommitProblemWithDetails {
            override val text: String
                get() = "Hey, this is a TODO" //TODO
            override val showDetailsAction: String
                get() = "Review" //TODO

            override fun showDetails(project: Project) {
                val converter = PylintMessageConverter(project)
                project.messageBus.syncPublisher(TreeListener.TOPIC).reinitialize(files)
                scanResults.map { converter.convert(it) }.forEach {
                    project.messageBus.syncPublisher(TreeListener.TOPIC).add(it)
                }
                ActivityTracker.getInstance().inc()
                PylintToolWindowPanel.getInstance(project).isVisible = true
            }
        }
    }
}