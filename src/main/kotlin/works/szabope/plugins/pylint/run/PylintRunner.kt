package works.szabope.plugins.pylint.run

import com.intellij.codeWithMe.ClientId
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.util.concurrency.AppExecutorUtil
import com.jetbrains.python.run.PythonCommandLineState
import com.jetbrains.python.run.PythonRunner
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import javax.swing.JComponent

class PylintRunner : PythonRunner() {

    override fun getRunnerId() = "works.szabope.plugins.pylint.run.PylintRunner"
    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return profile is PylintRunConfiguration
    }

    override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
        FileDocumentManager.getInstance().saveAllDocuments()
        val promise: AsyncPromise<RunContentDescriptor?> = AsyncPromise()
        execute(state, (Runnable {
            try {
                val executionResult = if (state is PythonCommandLineState) {
                    state.execute(environment.executor)
                } else {
                    state.execute(environment.executor, this)
                }
                ApplicationManager.getApplication()
                    .invokeLater({ promise.setResult(x(executionResult, environment)) }, ModalityState.any())
            } catch (e: ExecutionException) {
                promise.setError(e)
            }
        }))
        return promise
    }

    private fun x(executionResult: ExecutionResult?, environment: ExecutionEnvironment): RunContentDescriptor? {
        return executionResult?.let {
            createDescriptor(it, environment)
        }
    }

    private fun createDescriptor(
        executionResult: ExecutionResult,
        environment: ExecutionEnvironment
    ): RunContentDescriptor {
        val fakeJComponent = object : JComponent() {}
        val console = executionResult.executionConsole
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return RunContentDescriptor(console, executionResult.processHandler, fakeJComponent, "P-Y-L-I-N-T")
        }
        val contentDescriptor =
            object : RunContentDescriptor(console, executionResult.processHandler, fakeJComponent, "P-Y-L-I-N-T") {
                override fun isHiddenContent() = true
            }
        return contentDescriptor
    }

    private fun execute(profileState: RunProfileState, runnable: Runnable) {
        val clientIdRunnable: Runnable = ClientId.decorateRunnable(runnable)
        if (profileState is PythonCommandLineState) {
            AppExecutorUtil.getAppExecutorService().execute(clientIdRunnable)
        } else {
            ApplicationManager.getApplication().invokeAndWait(clientIdRunnable)
        }
    }
}
