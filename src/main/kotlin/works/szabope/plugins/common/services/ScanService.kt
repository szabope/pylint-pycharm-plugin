package works.szabope.plugins.common.services

import com.intellij.openapi.vfs.VirtualFile
import works.szabope.plugins.common.services.tool.AbstractToolOutputHandler

interface ScanService<I : ToolResultItem> {
    fun scan(
        targets: Collection<VirtualFile>,
        configuration: ImmutableSettingsData,
        resultHandler: AbstractToolOutputHandler<I>
    )
}