package net.derfruhling.serenity.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class NativeName(vararg val names: String)
