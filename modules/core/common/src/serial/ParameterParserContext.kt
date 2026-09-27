package net.derfruhling.serenity.serial

import net.derfruhling.serenity.annotations.UsedByGeneratedCode
import kotlin.reflect.KClass
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

    inline fun <reified T> getParser(): ParameterParser<T> {
        @OptIn(UsedByGeneratedCode::class)
        return getParser(typeOf<T>())
    }

    init {
        registerParser { _, text -> text.toByte() }
        registerParser { _, text -> text.toShort() }
        registerParser { _, text -> text.toInt() }
        registerParser { _, text -> text.toLong() }
        registerParser { _, text -> text.toUByte() }
        registerParser { _, text -> text.toUShort() }
        registerParser { _, text -> text.toUInt() }
        registerParser { _, text -> text.toULong() }
        registerParser { _, text -> text.toBoolean() }
    }
}