package net.derfruhling.serenity.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class ExtendsWith(val kClass: KClass<*>)
