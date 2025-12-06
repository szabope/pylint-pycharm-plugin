package works.szabope.plugins.pylint.configurable

import works.szabope.plugins.pylint.PylintBundle
import java.io.File

class PylintConfigFileValidator {
    fun validateConfigFilePath(path: String?): String? {
        if (path == null) return null
        require(path.isNotBlank())
        val file = File(path)
        if (!file.exists()) {
            return PylintBundle.message("pylint.configuration.path_to_config_file.not_exists")
        }
        if (file.isDirectory) {
            return PylintBundle.message("pylint.configuration.path_to_config_file.is_directory")
        }
        return null
    }
}