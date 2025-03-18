package works.szabope.plugins.pylint.run

import com.intellij.openapi.vfs.VirtualFile
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.tool.ToolOutputHandler
import works.szabope.plugins.pylint.services.parser.PylintMessage
import works.szabope.plugins.pylint.services.parser.PylintResult

interface IPylintExecutor {
    suspend fun execute(
        configuration: ImmutableSettingsData,
        targets: Collection<VirtualFile>,
        resultHandler: ToolOutputHandler<PylintMessage, PylintResult> //TODO: abstract message and result
    )
}