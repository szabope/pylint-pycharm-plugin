package works.szabope.plugins.pylint.annotator

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.DocumentUtil
import com.intellij.util.io.delete
import works.szabope.plugins.pylint.PylintBundle
import works.szabope.plugins.pylint.services.PylintSettings
import works.szabope.plugins.pylint.services.ScanService
import works.szabope.plugins.pylint.services.parser.PylintMessage
import works.szabope.plugins.pylint.toRunConfiguration
import kotlin.io.path.pathString
import kotlin.io.path.writeText

internal class PylintAnnotator : ExternalAnnotator<PylintAnnotator.AnnotatorInfo, List<PylintMessage>>() {

    private val logger = logger<PylintAnnotator>()

    class AnnotatorInfo(val file: VirtualFile, val project: Project)

    override fun collectInformation(file: PsiFile): AnnotatorInfo {
        return AnnotatorInfo(file.virtualFile, file.project)
    }

    override fun doAnnotate(info: AnnotatorInfo): List<PylintMessage> {
        val settings = PylintSettings.getInstance(info.project)
        settings.ensureValid()
        if (!settings.isComplete()) {
            return emptyList()
        }
        val fileDocumentManager = FileDocumentManager.getInstance()
        val document = requireNotNull(fileDocumentManager.getCachedDocument(info.file)) {
            "Please, report this issue at https://github.com/szabope/pylint-pycharm-plugin/issues"
        }

        val tempFile = kotlin.io.path.createTempFile(prefix = "pycharm_pylint_", suffix = ".py")
        logger.debug("Temporary file ${tempFile.pathString} created for ${info.file.name}")
        try {
            tempFile.toFile().deleteOnExit()
            tempFile.writeText(document.charsSequence)
            val service = ScanService.getInstance(info.project)
            val runConfiguration = settings.toRunConfiguration()
            val virtualTempFile = requireNotNull(VirtualFileManager.getInstance().findFileByNioPath(tempFile))
            return service.scan(listOf(virtualTempFile), runConfiguration)
        } finally {
            tempFile.delete()
        }
    }

    override fun apply(file: PsiFile, annotationResult: List<PylintMessage>, holder: AnnotationHolder) {
        logger.debug("Pylint returned ${annotationResult.size} issues for ${file.virtualFile.canonicalPath}")
        val profile = InspectionProjectProfileManager.getInstance(file.project).currentProfile
        val severity = HighlightDisplayKey.findById(PylintBundle.message("pylint.inspection.id"))?.let {
            profile.getErrorLevel(it, file).severity
        } ?: HighlightSeverity.ERROR

        annotationResult.forEach { issue ->
            val psiElement = requireNotNull(file.findElementFor(issue)) { "Pylint result mismatch for $issue" }
            holder.newAnnotation(severity, issue.message).range(psiElement.textRange).create()
        }
    }

    override fun getPairedBatchInspectionShortName(): String {
        return PylintBundle.message("pylint.inspection.id")
    }

    private fun PsiFile.findElementFor(issue: PylintMessage): PsiElement? {
        val tabSize = CodeStyle.getFacade(this).tabSize
        val offset = DocumentUtil.calculateOffset(fileDocument, issue.line, issue.column, tabSize)
        return findElementAt(offset)
    }
}
