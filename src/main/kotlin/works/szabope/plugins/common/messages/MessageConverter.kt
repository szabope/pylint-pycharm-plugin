package works.szabope.plugins.common.messages

interface MessageConverter<in S, out T> {
    fun convert(message: S): T
}
