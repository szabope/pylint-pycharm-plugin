package works.szabope.plugins.pylint.run

import works.szabope.plugins.pylint.services.parser.IPylintOutputHandler

interface IPylintExecutor {
    suspend fun execute(
        configuration: ExecutorConfiguration,
        targets: List<String>,
        resultHandler: IPylintOutputHandler
    )
}