package net.derfruhling.serenity.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class RegisterPage(
    val path: String,
    val title: String = ""
)
