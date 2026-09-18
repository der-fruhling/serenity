package net.derfruhling.serenity.annotations

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.CONSTRUCTOR
)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    "This API is used to implement the page extension API and should not be used from user code (unless you're implementing the page extension API, in which case go ahead :D)",
    RequiresOptIn.Level.ERROR
)
@MustBeDocumented
annotation class PageExtensionImplementationApi()
