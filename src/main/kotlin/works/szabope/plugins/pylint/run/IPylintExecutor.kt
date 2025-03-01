package works.szabope.plugins.pylint.run

import com.intellij.openapi.vfs.VirtualFile
import works.szabope.plugins.pylint.services.parser.PylintMessage
import works.szabope.plugins.pylint.services.parser.PylintResult
import works.szabope.plugins.common.services.tool.ToolOutputHandler

interface IPylintExecutor {
    suspend fun execute(
        configuration: ExecutorConfiguration,
        targets: Collection<VirtualFile>,
        resultHandler: ToolOutputHandler<PylintMessage, PylintResult>
    )
}