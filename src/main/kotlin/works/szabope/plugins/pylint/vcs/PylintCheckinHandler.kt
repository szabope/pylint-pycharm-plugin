package works.szabope.plugins.pylint.vcs

import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitExecutor
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.ui.RefreshableOnComponent
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.PairConsumer
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.dialog.IDialogManager
import works.szabope.plugins.pylint.services.ScanService
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.toRunConfiguration
import works.szabope.plugins.pylint.toolWindow.PylintToolWindowPanel
import works.szabope.plugins.pylint.toolWindow.getPylintPanel
import javax.swing.JCheckBox
import javax.swing.JComponent

class PylintCheckinHandler(private val panel: CheckinProjectPanel) : CheckinHandler() {

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

    override fun beforeCheckin(
        executor: CommitExecutor?, additionalDataConsumer: PairConsumer<Any, Any>?
    ): ReturnResult? {
        if (!settings.isScanBeforeCheckIn) return ReturnResult.COMMIT
        val filePaths = panel.virtualFiles.map { it.path }
        val runConfiguration = PylintSettings.getInstance(panel.project).toRunConfiguration()
        val scanResults =
            runWithModalProgressBlocking(panel.project, PylintBundle.message("pylint.checkin-handler.in-progress")) {
                service.scan(filePaths, runConfiguration)
            }
        if (scanResults.isNotEmpty()) {
            val commitButtonText = (executor?.actionText ?: panel.commitActionName).trimEnd('.')
            val dialog = IDialogManager.showPreCheckinConfirmationDialog(
                panel.project, scanResults.size, commitButtonText
            )
            when (dialog.getExitCode()) {
                Messages.OK -> {
                    getPylintPanel(panel.project)?.apply {
                        initializeResultTree(filePaths)
                        scanResults.forEach { addScanResult(it) }
                        ToolWindowManager.getInstance(panel.project).getToolWindow(PylintToolWindowPanel.ID)?.show()
                    }
                    return ReturnResult.CLOSE_WINDOW
                }

                Messages.CANCEL -> {
                    return ReturnResult.CANCEL
                }
            }
        }
        return super.beforeCheckin(executor, additionalDataConsumer)
    }
}