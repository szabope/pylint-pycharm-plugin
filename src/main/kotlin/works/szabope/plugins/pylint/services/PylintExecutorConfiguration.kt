package works.szabope.plugins.pylint.services

import works.szabope.plugins.common.services.ImmutableSettingsData

data class PylintExecutorConfiguration(
    override val executablePath: String?,
    override val useProjectSdk: Boolean,
    override val configFilePath: String? = null,
    override val arguments: String? = null,
    override val projectDirectory: String,
    override val excludeNonProjectFiles: Boolean,
    override val scanBeforeCheckIn: Boolean
) : ImmutableSettingsData