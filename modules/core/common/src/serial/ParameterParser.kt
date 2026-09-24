package net.derfruhling.serenity.serial

fun interface ParameterParser<T> {
    fun parse(fieldName: String, text: String): T
}
