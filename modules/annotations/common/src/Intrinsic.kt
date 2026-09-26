package net.derfruhling.serenity.annotations

@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class Intrinsic
