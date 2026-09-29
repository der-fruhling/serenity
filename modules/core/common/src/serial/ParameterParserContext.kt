package net.derfruhling.serenity.serial

import net.derfruhling.serenity.annotations.UsedByGeneratedCode
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class ParameterParserContext {
    private val parsers = mutableMapOf<KType, ParameterParser<*>>()

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    @UsedByGeneratedCode
    internal fun <T> getParser(kType: KType): ParameterParser<T> {
        return (parsers[kType] ?: throw IllegalStateException("type $kType has no registered parameter parser")) as ParameterParser<T>
    }

    @PublishedApi
    internal fun <T> registerParser(type: KType, parser: ParameterParser<T>) {
        parsers[type] = parser
    }

    inline fun <reified T> registerParser(parser: ParameterParser<T>) {
        registerParser(typeOf<T>(), parser)
    }

    inline fun <reified T> registerParser(crossinline parser: (String) -> T) {
        registerParser(typeOf<T>()) { _, text -> parser(text) }
    }

    inline fun <reified T> getParser(): ParameterParser<T> {
        @OptIn(UsedByGeneratedCode::class)
        return getParser(typeOf<T>())
    }

    init {
        registerParser(String::toByte)
        registerParser(String::toShort)
        registerParser(String::toInt)
        registerParser(String::toLong)
        registerParser(String::toUByte)
        registerParser(String::toUShort)
        registerParser(String::toUInt)
        registerParser(String::toULong)
        registerParser(String::toBoolean)
    }
}