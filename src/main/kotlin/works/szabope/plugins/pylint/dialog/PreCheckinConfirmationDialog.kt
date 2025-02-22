package works.szabope.plugins.pylint.dialog

import com.intellij.CommonBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageConstants.*
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.UIUtil
import works.szabope.plugins.pylint.PylintBundle

class PreCheckinConfirmationDialog(
    private val project: Project, private val errorCount: Int, private val commitButtonText: String
) {
    fun toPylintDialog() = object : PylintDialog {
        private var exitCode: Int? = null

        override fun show() {
            val buttons = arrayOf(
                PylintBundle.message("dialog.pre-checkin-confirmation.review"),
                commitButtonText,
                CommonBundle.getCancelButtonText()
            )
            val answer = Messages.showDialog(
                project,
                PylintBundle.message("dialog.pre-checkin-confirmation.text", errorCount),
                PylintBundle.message("dialog.pre-checkin-confirmation.title"),
                buttons,
                0,
                UIUtil.getWarningIcon()
            )
            exitCode = when (answer) {
                0 -> YES
                1 -> NO
                else -> CANCEL
            }
        }

        override fun getExitCode(): Int {
            return requireNotNull(exitCode) {
                "Please, report this issue at https://github.com/szabope/pylint-pycharm-plugin/issues"
            }
        }
    }
}