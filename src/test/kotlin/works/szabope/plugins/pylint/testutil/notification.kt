package works.szabope.plugins.pylint.testutil

import com.intellij.notification.Notification
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.test.notification.getConfigurationNotCompleteNotification
import works.szabope.plugins.pylint.PylintBundle

fun getConfigurationNotCompleteNotification(project: Project): Notification =
    getConfigurationNotCompleteNotification(
        project,
        PylintBundle.message("notification.group.pylint.group"),
        PylintBundle.message("pylint.notification.incomplete_configuration")
    )
