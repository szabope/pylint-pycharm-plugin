package works.szabope.plugins.pylint.services.parser

class CollectingOutputHandler : AbstractOutputHandler() {
    private val results = mutableListOf<PylintMessage>()

    override suspend fun handleResult(message: PylintMessage) {
        results.add(message)
    }

    fun getResults() = results.toList()
}
