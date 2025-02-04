package works.szabope.plugins.pylint.services.cli

import com.github.pgreze.process.Redirect
import com.github.pgreze.process.process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture

class StdoutHandler {

    val stdout = CompletableFuture<String>()

    suspend fun handle(flow: Flow<String>) {
        val stdoutBuilder = StringBuilder()
        flow.onCompletion { stdout.complete(stdoutBuilder.toString()) }.collect { stdoutBuilder.appendLine(it) }
    }
}

object Cli {
    class Status(val resultCode: Int, private val stderrLines: List<String>, val stdout: String) {
        val stderr: String get() = stderrLines.joinToString("\n")
    }

    suspend fun execute(vararg command: String, workDir: String? = null, env: Map<String, String>? = null): Status {
        require(command.isNotEmpty())
        val directory = workDir?.let { java.io.File(workDir) }
        val handler = StdoutHandler()
        return withContext(Dispatchers.IO) {
            val result = process(
                command = command,
                stdout = Redirect.Consume { handler.handle(it) },
                stderr = Redirect.CAPTURE,
                directory = directory,
                env = env
            )
            val stdout = handler.stdout.await()
            Status(result.resultCode, result.output, stdout)
        }
    }
}
