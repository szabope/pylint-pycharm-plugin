package works.szabope.plugins.pylint.services.parser

interface IPylintOutputHandler {
    suspend fun handle(result: PylintResult)
}
