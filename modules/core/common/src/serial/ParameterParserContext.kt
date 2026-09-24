package net.derfruhling.serenity.serial

import net.derfruhling.serenity.annotations.UsedByGeneratedCode
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class ParameterParserContext {
    @PublishedApi
    @UsedByGeneratedCode
    internal fun <T> getParser(kType: KType): ParameterParser<T> {
        TODO()
    }

    inline fun <reified T> getParser(): ParameterParser<T> {
        @OptIn(UsedByGeneratedCode::class)
        return getParser(typeOf<T>())
    }
}