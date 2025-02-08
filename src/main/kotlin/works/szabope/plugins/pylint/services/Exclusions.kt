package works.szabope.plugins.pylint.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import kotlin.io.path.Path

class Exclusions(private val project: Project) {
    fun findAll(targets: List<String>): List<String> {
        val workspaceModel = WorkspaceModel.getInstance(project)
        return targets.flatMap { target ->
            val targetUrl = workspaceModel.getVirtualFileUrlManager().fromPath(target)
            workspaceModel.currentSnapshot.getVirtualFileUrlIndex().findEntitiesByUrl(targetUrl)
                .filter { it is ContentRootEntity }.map { it as ContentRootEntity }.flatMap { it.excludedUrls }
                .mapNotNull { it.url.virtualFile?.path }.map { Path(it).toCanonicalPath() }
        }
    }
}