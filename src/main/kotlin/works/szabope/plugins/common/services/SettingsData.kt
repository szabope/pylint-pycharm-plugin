package works.szabope.plugins.common.services

interface SettingsData : BasicSettingsData {
    val useProjectSdk: Boolean
    val projectDirectory: String?
    val excludeNonProjectFiles: Boolean
}