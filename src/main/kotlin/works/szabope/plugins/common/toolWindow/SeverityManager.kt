package works.szabope.plugins.common.toolWindow

class SeverityManager(severities: Set<String>) {
    private val displayedSeverityLevels = severities.toMutableSet()
    private val listeners = mutableListOf<() -> Unit>()

    fun isSeverityLevelDisplayed(severityLevel: String): Boolean {
        return displayedSeverityLevels.contains(severityLevel)
    }

    fun setSeverityLevelDisplayed(severityLevel: String, isDisplayed: Boolean): Boolean {
        val hadEffect = if (isDisplayed) {
            displayedSeverityLevels.add(severityLevel)
        } else {
            displayedSeverityLevels.remove(severityLevel)
        }
        if (hadEffect) {
            listeners.forEach { it() }
        }
        return hadEffect
    }

    fun addChangeListener(listener: () -> Unit) {
        listeners.add(listener)
    }
}
