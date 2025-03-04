package works.szabope.plugins.common.services

@JvmInline
value class SettingsValidationProblem(val message: String) {
    override fun toString() = message
}

interface Settings {
    var executablePath: String?
    var useProjectSdk: Boolean
    var configFilePath: String?
    var arguments: String?
    var projectDirectory: String?
    var isExcludeNonProjectFiles: Boolean
    fun validateExecutable(path: String?): SettingsValidationProblem?
    fun validateSdk(): SettingsValidationProblem?
    fun validateConfigFile(path: String?): SettingsValidationProblem?
    fun validateProjectDirectory(path: String?): SettingsValidationProblem?
}