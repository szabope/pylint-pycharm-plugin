package works.szabope.plugins.pylint.run

import com.intellij.openapi.vfs.VirtualFile
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.tool.ToolOutputHandler

interface IPylintExecutor {
    suspend fun execute(
        configuration: ImmutableSettingsData,
        targets: Collection<VirtualFile>,
        resultHandler: ToolOutputHandler
    ): Result<Unit>
}