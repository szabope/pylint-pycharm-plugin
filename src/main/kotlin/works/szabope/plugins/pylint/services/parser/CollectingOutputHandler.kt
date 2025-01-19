package works.szabope.plugins.pylint.services.parser

class CollectingOutputHandler : AbstractOutputHandler() {
    private val results = mutableListOf<PylintMessage>()

    override suspend fun handleResult(result: PylintMessage) {
        results.add(result)
    }

    fun getResults() = results.toList()
}
