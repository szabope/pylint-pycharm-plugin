package works.szabope.plugins.pylint.annotator

import com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection
import com.jetbrains.python.inspections.PyInspection
import works.szabope.plugins.pylint.PylintBundle

internal class PylintInspection : PyInspection(), ExternalAnnotatorBatchInspection {

    override fun getShortName(): String {
        return PylintBundle.message("pylint.inspection.id")
    }
}
