package works.szabope.plugins.pylint.messages

interface MessageConverter<in S, out T> {
    fun convert(source: S): T
}
