package works.szabope.plugins.pylint.initialization

import works.szabope.plugins.pylint.AbstractToolWindowTestCase
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.testutil.getConfigurationNotCompleteNotification

class PluginInitializationFromScratchTest : AbstractToolWindowTestCase() {

    fun `test plugin initialized from scratch (no python sdk) results in notification`() {
        val actions = getConfigurationNotCompleteNotification(project).actions
        assertEquals(
            PylintBundle.message("action.works.szabope.plugins.pylint.action.OpenSettingsAction.text"),
            actions.single().templatePresentation.text
        )
    }
}