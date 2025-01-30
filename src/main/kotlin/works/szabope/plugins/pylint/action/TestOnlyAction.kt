package works.szabope.plugins.pylint.action

import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.jetbrains.python.sdk.pythonSdk
import works.szabope.plugins.pylint.run.PylintConfigurationType
import works.szabope.plugins.pylint.run.PylintRunner
import works.szabope.plugins.pylint.services.PylintService

class TestOnlyAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project!!
        val configurationFactory = PylintConfigurationType.INSTANCE.getFactory()
        val conf = configurationFactory.createConfiguration(project, "pylint")
        val workDir = project.basePath!!
        conf.sdk = project.pythonSdk
        conf.workingDirectory = workDir
        conf.isPassParentEnvs = true // TODO: this is default
        conf.isUseModuleSdk = false // TODO: this is default
        conf.setAddContentRoots(true)
        conf.setAddSourceRoots(true)
        conf.scriptName = "pylint"
        conf.scriptParameters = """
            --jobs 0 --recursive y 
            --ignore-paths $workDir/.venv 
            --exit-zero 
            --output-format json2 
            $workDir""".trimIndent()
        conf.setShowCommandLineAfterwards(false) // TODO: this is default
        conf.setEmulateTerminal(false) // TODO: this is default
        conf.isModuleMode = true
        conf.isRedirectInput = false // TODO: this is default
        conf.collectOutputFromProcessHandler()
        val settings = RunManager.getInstance(project).createConfiguration(
            conf, configurationFactory
        ) as RunnerAndConfigurationSettingsImpl
        settings.isActivateToolWindowBeforeRun = false
        val executor = DefaultRunExecutor.getRunExecutorInstance();
        val environment =
            ExecutionEnvironmentBuilder.create(executor, settings).runner(PylintRunner()).build()
        PylintService.getInstance(project).scanWithSdkAsync(environment)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
