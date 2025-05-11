package works.szabope.plugins.common.services

interface BasicSettingsData {
    val executablePath: String?
    val configFilePath: String?
    val arguments: String?
    val scanBeforeCheckIn: Boolean
}