package net.derfruhling.serenity.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
@UsedFromCompilerPlugin
annotation class Page(
    val path: String
)
