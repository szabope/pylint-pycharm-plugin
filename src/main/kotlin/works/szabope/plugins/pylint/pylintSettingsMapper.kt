package works.szabope.plugins.pylint

import works.szabope.plugins.pylint.run.ExecutorConfiguration
import works.szabope.plugins.pylint.services.PylintSettings

fun PylintSettings.toRunConfiguration() = ExecutorConfiguration(
    executablePath!!,
    useProjectSdk,
    configFilePath,
    arguments,
    isExcludeNonProjectFiles,
    customExclusions,
    projectDirectory!!
)
