package works.szabope.plugins.pylint

import com.intellij.notification.ActionCenter
import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.testFramework.TestDataPath
import com.intellij.webcore.packaging.PackageManagementService
import io.mockk.*
import works.szabope.plugins.pylint.action.OpenSettingsAction
import works.szabope.plugins.pylint.services.PylintPackageUtil
import works.szabope.plugins.pylint.testutil.PackageManagementTestService

@TestDataPath("\$CONTENT_ROOT/testData/incompleteConfigurationNotification")
class PylintIncompleteConfigurationNotificationTest : AbstractToolWindowTestCase() {

    override fun getTestDataPath() = "src/test/testData/incompleteConfigurationNotification"

    override fun tearDown() {
        clearAllMocks()
        unmockkAll()
        super.tearDown()
    }

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
        val packageManager = mockPackageManager(true)
        PylintSettingsInitializationTestService.getInstance(project).executeInitialization()
        val notification = getSettingsNotification()
        val actions = notification.actions
        assertEquals(2, actions.size)
        val action = AnActionWrapper(actions.last()) // Install pylint action
        val event = getAnActionEvent(notification)
        action.update(event)
        assertTrue(event.presentation.isEnabled)
        action.actionPerformed(event)
        assertNotEmpty(packageManager.installedPackagesList.filter { it.name == "pylint" })
    }

    fun testRemoteSdkNotification() {
        mockPackageManager(false)
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

    private fun mockPackageManager(canInstall: Boolean): PackageManagementService {
        val packageManager = PackageManagementTestService()
        mockkObject(PylintPackageUtil)
        every { PylintPackageUtil.getPackageManager(project) } returns packageManager
        every { PylintPackageUtil.canInstall(project) } returns canInstall
        return packageManager
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
