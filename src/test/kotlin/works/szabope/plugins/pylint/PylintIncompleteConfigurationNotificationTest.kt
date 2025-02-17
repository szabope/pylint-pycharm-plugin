package works.szabope.plugins.pylint

import com.intellij.notification.ActionCenter
import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.testFramework.TestDataPath
import com.jetbrains.python.packaging.management.PythonPackageManager
import com.jetbrains.python.sdk.pythonSdk
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.pylint.action.OpenSettingsAction
import works.szabope.plugins.pylint.testutil.PythonMockSdk
import works.szabope.plugins.pylint.testutil.TestPythonPackageManager
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@Suppress("OverrideOnly", "UnstableApiUsage")
@TestDataPath("\$CONTENT_ROOT/testData/notification")
class PylintIncompleteConfigurationNotificationTest : AbstractToolWindowTestCase() {

    override fun getTestDataPath() = "src/test/testData/notification"

    fun testNoSdkNotification() {
        val openSettingsAction = mockOpenSettingsAction()
        val notification = getSettingsNotification()
        val actions = notification.actions
        assertEquals(1, actions.size)
        val action = AnActionWrapper(actions.first()) // Complete configuration action
        val event = getAnActionEvent(notification)
        action.update(event)
        assertTrue(event.presentation.isEnabled)
        action.actionPerformed(event)
        verify {
            openSettingsAction.actionPerformed(any(AnActionEvent::class))
        }
    }

    fun testLocalSdkNotification() {
        val mockSdk = PythonMockSdk.create(
            "${Paths.get(testDataPath).absolutePathString()}/MockSdk"
        )
        val packageManager = TestPythonPackageManager(project, mockSdk)
        mockkObject(PythonPackageManager)
        every { PythonPackageManager.forSdk(any(), any()) } returns packageManager
        runWriteActionAndWait {
            ProjectJdkTable.getInstance().addJdk(mockSdk)
        }
        project.pythonSdk = mockSdk
        module.pythonSdk = mockSdk
        runBlocking {
            triggerReconfiguration()
        }
        try {
            val notification = getSettingsNotification()
            val actions = notification.actions
            assertEquals(2, actions.size)
            val action = AnActionWrapper(actions.last()) // Install pylint action
            val event = getAnActionEvent(notification)
            action.update(event)
            assertTrue(event.presentation.isEnabled)
            action.actionPerformed(event)
            assertNotEmpty(packageManager.installedPackages.filter { it.name == "pylint" })
        } finally {
            project.pythonSdk = null
            module.pythonSdk = null
            runWriteActionAndWait {
                ProjectJdkTable.getInstance().removeJdk(mockSdk)
            }
        }
    }

    fun testRemoteSdkNotification() { //TODO: actually test with something that appears to be a remote sdk
        val openSettingsAction = mockOpenSettingsAction()
        val notification = getSettingsNotification()
        val actions = notification.actions
        assertEquals(1, actions.size)
        val action = AnActionWrapper(actions.first()) // Complete configuration action
        val event = getAnActionEvent(notification)
        action.update(event)
        assertTrue(event.presentation.isEnabled)
        action.actionPerformed(event)
        verify {
            openSettingsAction.actionPerformed(any(AnActionEvent::class))
        }
    }

    private fun mockOpenSettingsAction(): AnAction {
        val openSettingsAction = ActionManager.getInstance().getAction(OpenSettingsAction.ID)
        mockkObject(openSettingsAction)
        every { openSettingsAction.actionPerformed(any()) } returns Unit
        return openSettingsAction
    }

    private fun getAnActionEvent(notification: Notification): AnActionEvent {
        val context =
            SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).add(Notification.KEY, notification).build()
        return AnActionEvent.createEvent(context, null, ActionPlaces.NOTIFICATION, ActionUiKind.NONE, null)
    }

    private fun getSettingsNotification(): Notification {
        val notifications = ActionCenter.getNotifications(project).filter {
            "Pylint Group" == it.groupId && PylintBundle.message("pylint.settings.incomplete") == it.content && !it.isExpired
        }
        return notifications.single()
    }
}
