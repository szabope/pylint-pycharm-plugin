package works.szabope.plugins.pylint.annotator

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.services.ScanService
import works.szabope.plugins.pylint.services.parser.PylintMessage

internal class PylintAnnotator : ToolAnnotator<PylintMessage>() {
    override fun getSettingsInstance(project: Project) = PylintSettings.getInstance(project)

    override fun scan(
        info: AnnotatorInfo, configuration: ImmutableSettingsData
    ) = ScanService.getInstance(info.project).scan(listOf(info.file), configuration)

    override val inspectionId = "PylintInspection"

    override fun createIntention(message: PylintMessage) = null
}