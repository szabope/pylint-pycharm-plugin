package works.szabope.plugins.pylint.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.project.Project
import com.jetbrains.python.run.PythonConfigurationFactoryBase
import com.jetbrains.python.run.PythonRunConfiguration
import java.awt.Component
import java.awt.Graphics
import javax.swing.Icon

class DummyIcon(private val size: Int) : Icon {
    override fun paintIcon(p0: Component?, p1: Graphics?, p2: Int, p3: Int) = Unit
    override fun getIconWidth() = size
    override fun getIconHeight() = size
}

class PylintConfigurationType : ConfigurationType {
    private val myFactory = PylintConfigurationFactory(this)

    // this thing should never appear on screen
    override fun getDisplayName() = "Pylint"
    override fun getConfigurationTypeDescription() = "Pylint run configuration"
    override fun getIcon() = DummyIcon(16)

    override fun getId() = "Pylint"
    override fun isManaged() = false
    override fun getConfigurationFactories() = arrayOf<ConfigurationFactory>(myFactory)

    fun getFactory() = myFactory

    companion object {
        @JvmStatic
        val INSTANCE = PylintConfigurationType()
    }
}

class PylintConfigurationFactory(type: ConfigurationType) : PythonConfigurationFactoryBase(type) {
    fun createConfiguration(project: Project, name: String) = PylintRunConfiguration(project, this, name)
    override fun createTemplateConfiguration(project: Project) = createConfiguration(project, "Pylint Template")
    override fun getId() = "Pylint"
}

class PylintRunConfiguration(project: Project, factory: ConfigurationFactory, configurationName: String) :
    PythonRunConfiguration(project, factory) {

    init {
        setUnbufferedEnv()
        name = configurationName
    }

    override fun checkConfiguration() = Unit
}
