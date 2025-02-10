package works.szabope.plugins.pylint.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ExcludeUrlEntity
import kotlin.io.path.Path

class Exclusions(private val project: Project) {
    fun findAll(targets: List<String>): List<String> {
        //TODO: filter relevant ones for given targets
        return WorkspaceModel.getInstance(project).currentSnapshot.entities(ExcludeUrlEntity::class.java)
            .mapNotNull { it.url.virtualFile?.path }.map { Path(it).toCanonicalPath() }.toList()
    }
}
