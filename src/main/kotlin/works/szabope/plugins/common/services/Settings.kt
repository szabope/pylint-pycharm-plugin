package works.szabope.plugins.common.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@JvmInline
value class SettingsValidationProblem(val message: String) {
    override fun toString() = message
}

interface Settings : SettingsData {
    var isAutoScrollToSource: Boolean
    override var scanBeforeCheckIn: Boolean
    override var excludeNonProjectFiles: Boolean
    override var projectDirectory: String?
    override var useProjectSdk: Boolean
    override var arguments: String?
    override var configFilePath: String?
    override var executablePath: String?

    fun validateExecutable(path: String?): SettingsValidationProblem?
    fun validateSdk(): SettingsValidationProblem?
    fun validateConfigFile(path: String?): SettingsValidationProblem?
    fun validateProjectDirectory(path: String?): SettingsValidationProblem?
    suspend fun initSettings(oldSettings: BasicSettingsData?)
    fun ensureValid(): SettingsValidationProblem?
    fun isComplete(): Boolean
    fun getData(): ImmutableSettingsData
    fun reset()
    suspend fun autodetectExecutable(): String?

    companion object {
        @JvmStatic
        fun getInstance(project: Project): Settings = project.service()
    }
}