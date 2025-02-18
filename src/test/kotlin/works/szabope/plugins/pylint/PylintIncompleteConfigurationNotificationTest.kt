package works.szabope.plugins.pylint

import com.intellij.notification.ActionCenter
import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.testFramework.TestDataPath
import com.jetbrains.python.remote.PyRemoteSdkAdditionalData
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.pylint.action.OpenSettingsAction
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

    fun testLocalSdkNotification() =
        withMockSdk("${Paths.get(testDataPath).absolutePathString()}/MockSdk") { packageManager ->
            val notification = getSettingsNotification()
            val actions = notification.actions
            assertEquals(2, actions.size)
            val action = AnActionWrapper(actions.last()) // Install pylint action
            val event = getAnActionEvent(notification)
            action.update(event)
            assertTrue(event.presentation.isEnabled)
            action.actionPerformed(event)
            assertNotEmpty(packageManager.installedPackages.filter { it.name == "pylint" })
        }

    fun testRemoteSdkNotification() =
        withMockSdk("${Paths.get(testDataPath).absolutePathString()}/MockSdk") { packageManager ->
            val mockSdk = packageManager.sdk
            // let's lie about locality, see com.jetbrains.python.sdk.PythonSdkUtil#isRemote(Sdk)
            mockkObject(mockSdk)
            every { mockSdk.sdkAdditionalData } returns PyRemoteSdkAdditionalData(null)
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
        runBlocking {
            triggerReconfiguration()
        }
        val notifications = ActionCenter.getNotifications(project).filter {
            "Pylint Group" == it.groupId && PylintBundle.message("pylint.settings.incomplete") == it.content && !it.isExpired
        }
        return notifications.single()
    }
}
