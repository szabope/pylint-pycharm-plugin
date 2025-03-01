package works.szabope.plugins.common.services

interface SeverityConfigService {
    fun getAll(): Set<SeverityConfig>
    fun filter(filter: ((String) -> Boolean)) = getAll().filter { filter(it.level) }
    fun findByType(type: String, lazyMessage: () -> String) = try {
        filter { it == type }.single()
    } catch (e: NoSuchElementException) {
        throw NoSuchElementException(lazyMessage(), e)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException(lazyMessage(), e)
    }
}