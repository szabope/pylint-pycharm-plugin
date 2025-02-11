package works.szabope.plugins.pylint.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.toVirtualFileUrl
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ExcludeUrlEntity
import com.intellij.workspaceModel.ide.isEqualOrParentOf
import kotlin.io.path.Path

class Exclusions(private val project: Project) {
    fun findAll(targets: Collection<VirtualFile>): List<String> {
        val workspaceModel = WorkspaceModel.getInstance(project)
        val targetUrls = targets.map { target -> target.toVirtualFileUrl(workspaceModel.getVirtualFileUrlManager()) }
        return workspaceModel.currentSnapshot.entities(ExcludeUrlEntity::class.java)
            .filter { excluded -> targetUrls.any { it.isEqualOrParentOf(excluded.url) } }
            .mapNotNull { it.url.virtualFile?.path }.map { Path(it).toCanonicalPath() }.toList()
    }
}
