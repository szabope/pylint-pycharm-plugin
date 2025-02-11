package works.szabope.plugins.pylint.run

import com.intellij.openapi.vfs.VirtualFile
import works.szabope.plugins.pylint.services.parser.IPylintOutputHandler

interface IPylintExecutor {
    suspend fun execute(
        configuration: ExecutorConfiguration,
        targets: Collection<VirtualFile>,
        resultHandler: IPylintOutputHandler
    )
}