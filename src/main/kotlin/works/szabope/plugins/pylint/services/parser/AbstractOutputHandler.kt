package works.szabope.plugins.pylint.services.parser

abstract class AbstractOutputHandler : IPylintOutputHandler {

    abstract suspend fun handleResult(message: PylintMessage)

    override suspend fun handle(result: PylintResult) {
        result.messages.forEach { message ->
            handleResult(message)
        }
    }
}
