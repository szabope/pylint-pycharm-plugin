package works.szabope.plugins.pylint.annotator

import com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection
import com.jetbrains.python.inspections.PyInspection

internal class PylintInspection : PyInspection(), ExternalAnnotatorBatchInspection {
    // Mind plugin.xml when changing the value of pylint.inspection.id
    override fun getShortName() = "PylintInspection"
}
