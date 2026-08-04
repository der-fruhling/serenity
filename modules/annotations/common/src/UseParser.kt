package net.derfruhling.serenity.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class UseParser<R, T : Parser<R>>(val parserClass: KClass<T>)

