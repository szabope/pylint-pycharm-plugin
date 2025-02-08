package works.szabope.plugins.pylint.run

class ExecutorConfiguration(
    val executablePath: String,
    val useProjectSdk: Boolean,
    val configFilePath: String? = null,
    val arguments: String? = null,
    val excludeNonProjectFiles: Boolean = true,
    val customExclusions: List<String> = listOf(),
    val projectDirectory: String
)