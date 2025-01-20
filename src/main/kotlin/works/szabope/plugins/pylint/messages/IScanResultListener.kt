package works.szabope.plugins.pylint.messages

import works.szabope.plugins.pylint.services.parser.PylintMessage

fun interface IScanResultListener {
    fun process(result: PylintMessage)
}
