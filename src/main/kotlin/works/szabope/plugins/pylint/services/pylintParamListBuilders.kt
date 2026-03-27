package works.szabope.plugins.pylint.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.util.execution.ParametersListUtil
import com.intellij.util.text.nullize
import works.szabope.plugins.common.run.Exclusions
import works.szabope.plugins.common.services.ToolExecutorConfiguration
import works.szabope.plugins.pylint.PylintArgs
import kotlin.io.path.Path

context(project: Project)
fun buildParamList(
    configuration: ToolExecutorConfiguration, targets: Collection<VirtualFile>
) = with(configuration) {
    val params = mutableListOf<String>()
    configFilePath.nullize(true)?.let { params.add("--rcfile"); params.add(it) }
    arguments.nullize(true)?.let { params.addAll(ParametersListUtil.parse(it)) }
    if (excludeNonProjectFiles) {
        Exclusions(project).findAll(targets).mapNotNull {
            it.url.virtualFile?.path?.let { p -> Path(p).toCanonicalPath() }
        }.joinToString(",").nullize()?.apply { params.add("--ignore-paths"); params.add(this) }
    }
    // mandatory args to take precedence
    params.addAll(PylintArgs.MANDATORY_ARGS)
    params.addAll(targets.map { requireNotNull(it.canonicalPath) })
    params
}
