package works.szabope.plugins.pylint

import works.szabope.plugins.pylint.services.PylintService
import works.szabope.plugins.pylint.services.PylintSettings

fun PylintSettings.toRunConfiguration() = PylintService.RunConfiguration(
    executablePath!!, configFilePath, arguments, isExcludeNonProjectFiles, customExclusions, projectDirectory!!
)
