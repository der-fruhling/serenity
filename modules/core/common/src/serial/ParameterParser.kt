package net.derfruhling.serenity.serial

import net.derfruhling.serenity.annotations.UsedByGeneratedCode

fun interface ParameterParser<T> {
    @UsedByGeneratedCode
    fun parse(fieldName: String, text: String): T
}
