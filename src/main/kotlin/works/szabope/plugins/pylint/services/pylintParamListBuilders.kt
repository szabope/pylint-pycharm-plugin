package works.szabope.plugins.pylint.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ExcludeUrlEntity
import com.intellij.util.execution.ParametersListUtil
import com.intellij.util.text.nullize
import works.szabope.plugins.common.run.Exclusions
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.pylint.PylintArgs
import java.nio.file.Path
import kotlin.io.path.Path

context(project: Project)
fun buildParamList(
    configuration: ImmutableSettingsData, targets: Collection<VirtualFile>
) = with(configuration) {
    val params = mutableListOf<String>()
    configFilePath.nullize(true)?.let { params.add("--rcfile"); params.add(it) }
    arguments.nullize(true)?.let { params.addAll(ParametersListUtil.parse(it)) }
    if (excludeNonProjectFiles) {
        Exclusions(project).findAll(targets).mapNotNull {
            getRelativePathFromContentRoot(it)?.toCanonicalPath()
        }.joinToString(",").nullize()?.apply { params.add("--ignore-paths"); params.add(this) }
    }
    // mandatory args to take precedence
    params.addAll(PylintArgs.PYLINT_MANDATORY_COMMAND_ARGS.split(" "))
    targets.map { requireNotNull(it.canonicalPath) }.let { params.addAll(it) }
    params
}

private fun getRelativePathFromContentRoot(excludeUrlEntity: ExcludeUrlEntity): Path? {
    return excludeUrlEntity.url.virtualFile?.path?.let { Path(it) }
}
