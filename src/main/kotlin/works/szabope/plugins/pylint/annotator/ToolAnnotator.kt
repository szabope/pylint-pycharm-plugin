package works.szabope.plugins.pylint.annotator

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.LightVirtualFile
import com.intellij.util.DocumentUtil
import fleet.util.letIfNotNull
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.pylint.services.parser.ToolMessage

abstract class ToolAnnotator<T : ToolMessage> : ExternalAnnotator<ToolAnnotator.AnnotatorInfo, List<T>>() {

    abstract fun getSettingsInstance(project: Project): Settings
    abstract fun scan(info: AnnotatorInfo, configuration: ImmutableSettingsData): List<T>
    abstract val inspectionId: String
    abstract fun createIntention(message: T): IntentionAction?

    class AnnotatorInfo(val file: VirtualFile, val project: Project)

    override fun collectInformation(file: PsiFile): AnnotatorInfo? {
        // do not run for in-memory files
        if (file.virtualFile is LightVirtualFile) return null
        return AnnotatorInfo(file.virtualFile, file.project)
    }

    override fun doAnnotate(info: AnnotatorInfo): List<T> {
        val configuration = getSettingsInstance(info.project).getValidConfiguration()
        if (configuration.isFailure) {
            return emptyList()
        }
        return scan(info, configuration.getOrThrow())
    }

    override fun apply(file: PsiFile, annotationResult: List<T>, holder: AnnotationHolder) {
        val profile = InspectionProjectProfileManager.getInstance(file.project).currentProfile
        val severity = HighlightDisplayKey.findById(inspectionId)?.let {
            profile.getErrorLevel(it, file).severity
        } ?: HighlightSeverity.ERROR
        annotationResult.map {
            it to requireNotNull(file.findElementFor(it)) { "Result mismatch for $it" }
        }.forEach { (issue, psiElement) ->
            holder.newAnnotation(severity, issue.message).range(psiElement.textRange)
                .letIfNotNull(createIntention(issue)) { annotator, intention -> annotator.withFix(intention) }.create()
        }
    }

    override fun getPairedBatchInspectionShortName(): String {
        return inspectionId
    }

    private fun PsiFile.findElementFor(issue: T): PsiElement? {
        val tabSize = CodeStyle.getFacade(this).tabSize
        val offset = DocumentUtil.calculateOffset(fileDocument, issue.line, issue.column, tabSize)
        return findElementAt(offset)
    }
}
