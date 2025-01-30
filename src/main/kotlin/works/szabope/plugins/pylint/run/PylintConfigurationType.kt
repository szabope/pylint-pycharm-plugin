package works.szabope.plugins.pylint.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.project.Project
import com.jetbrains.python.run.PythonConfigurationFactoryBase
import com.jetbrains.python.run.PythonRunConfiguration
import javax.swing.ImageIcon

class PylintConfigurationType : ConfigurationType {
    private val myFactory = PylintConfigurationFactory(this)

    override fun getDisplayName() = "Pylint"
    override fun getConfigurationTypeDescription() = "Pylint run configuration"
    override fun getIcon() = ImageIcon("icons/pylintScanAction.svg") //TODO: dummy
    override fun getId() = "Pylint"
    override fun getConfigurationFactories() = arrayOf<ConfigurationFactory>(myFactory);
    override fun isManaged() = false

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
}