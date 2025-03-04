package works.szabope.plugins.common.services

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.SimplePersistentStateComponent

@JvmInline
value class SettingsValidationProblem(val message: String) {
    override fun toString() = message
}

abstract class Settings<T : BaseState>(initialState: T) : SimplePersistentStateComponent<T>(initialState) {
    abstract var executablePath: String?
    abstract var useProjectSdk: Boolean
    abstract var configFilePath: String?
    abstract var arguments: String?
    abstract var projectDirectory: String?
    abstract var isExcludeNonProjectFiles: Boolean
    abstract fun validateExecutable(path: String?): SettingsValidationProblem?
    abstract fun validateSdk(): SettingsValidationProblem?
    abstract fun validateConfigFile(path: String?): SettingsValidationProblem?
    abstract fun validateProjectDirectory(path: String?): SettingsValidationProblem?
}