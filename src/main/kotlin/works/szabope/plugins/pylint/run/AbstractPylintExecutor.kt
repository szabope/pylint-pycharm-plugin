package works.szabope.plugins.pylint.run

import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import works.szabope.plugins.pylint.services.parser.IPylintOutputHandler
import kotlin.io.path.Path

//TODO: interface
abstract class AbstractPylintExecutor {
    abstract suspend fun execute(
        configuration: ExecutorConfiguration, targets: List<String>, resultHandler: IPylintOutputHandler
    )

    //TODO: move
    protected fun collectExclusionsFor(target: String, workspaceModel: WorkspaceModel): List<String> {
        val exclusions = mutableListOf<String>()
        val targetUrl = workspaceModel.getVirtualFileUrlManager().fromPath(target)
        workspaceModel.currentSnapshot.getVirtualFileUrlIndex().findEntitiesByUrl(targetUrl).forEach { entity ->
            if (entity is ContentRootEntity) {
                entity.excludedUrls.mapNotNull { it.url.virtualFile?.path }.map { Path(it) }
                    .forEach { exclusions.add(it.toCanonicalPath()) }
            }
        }
        return exclusions
    }

}