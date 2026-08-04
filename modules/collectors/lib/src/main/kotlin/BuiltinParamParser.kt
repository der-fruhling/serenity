package net.derfruhling.serenity.processor

import kotlin.reflect.KClass

enum class BuiltinParamParser(val typeName: String, val parse: String, val encode: String = "it.toString()") {
    STRING(String::class, "it"),
    BYTE(Byte::class, "it.toByte()"),
    U_BYTE(UByte::class, "it.toUByte()"),
    SHORT(Short::class, "it.toShort()"),
    U_SHORT(UShort::class, "it.toUShort()"),
    INT(Int::class, "it.toInt()"),
    U_INT(UInt::class, "it.toUInt()"),
    LONG(Long::class, "it.toLong()"),
    U_LONG(ULong::class, "it.toULong()"),

    ;

    companion object {
        val all = entries.associateBy { it.typeName }
    }

    constructor(type: KClass<*>, parse: String, encode: String = "it.toString()")
        : this(type.qualifiedName!!, parse, encode)
}
