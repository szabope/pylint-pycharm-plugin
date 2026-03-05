package works.szabope.plugins.pylint.action

import com.intellij.openapi.options.BoundSearchableConfigurable
import works.szabope.plugins.common.action.AbstractOpenSettingsAction
import works.szabope.plugins.pylint.configurable.PylintConfigurable

class OpenSettingsAction : AbstractOpenSettingsAction() {
    override fun getConfigurableClass(): Class<out BoundSearchableConfigurable> = PylintConfigurable::class.java

    companion object {
        const val ID = "works.szabope.plugins.pylint.action.OpenSettingsAction"
    }
}
