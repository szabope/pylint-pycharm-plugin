package works.szabope.plugins.pylint.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.util.concurrency.AppExecutorUtil
import com.jetbrains.python.run.PythonCommandLineState
import com.jetbrains.python.run.PythonRunner
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import javax.swing.JComponent

class PylintRunner private constructor() : PythonRunner() {

    override fun getRunnerId() = "works.szabope.plugins.pylint.run.PylintRunner"
    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return profile is PylintRunConfiguration
    }

    override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
        val promise: AsyncPromise<RunContentDescriptor?> = AsyncPromise()
        execute(state, (Runnable {
            try {
                val executionResult = if (state is PythonCommandLineState) {
                    state.execute(environment.executor)
                } else {
                    state.execute(environment.executor, this)
                }
                ApplicationManager.getApplication()
                    .invokeLater({ promise.setResult(executionResult?.let(::createDescriptor)) }, ModalityState.any())
            } catch (e: ExecutionException) {
                promise.setError(e)
            }
        }))
        return promise
    }

    private fun createDescriptor(executionResult: ExecutionResult): RunContentDescriptor {
        val fakeJComponent = object : JComponent() {}
        val console = executionResult.executionConsole
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return RunContentDescriptor(console, executionResult.processHandler, fakeJComponent, "P-Y-L-I-N-T")
        }
        val contentDescriptor =
            object : RunContentDescriptor(console, executionResult.processHandler, fakeJComponent, "P-Y-L-I-N-T") {
                @Suppress("UnstableApiUsage")
                override fun isHiddenContent() = true
            }
        return contentDescriptor
    }

    private fun execute(profileState: RunProfileState, runnable: Runnable) {
        if (profileState is PythonCommandLineState) {
            AppExecutorUtil.getAppExecutorService().execute(runnable)
        } else {
            ApplicationManager.getApplication().invokeAndWait(runnable)
        }
    }

    companion object {
        @JvmStatic
        val INSTANCE = PylintRunner()
    }
}
