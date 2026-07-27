package net.derfruhling.html.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.EXPRESSION, AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
annotation class Client

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.EXPRESSION, AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
annotation class Server

@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.TYPEALIAS
)
@RequiresOptIn
@MustBeDocumented
@Client
annotation class ClientOnly

@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.TYPEALIAS
)
@RequiresOptIn
@MustBeDocumented
@Server
annotation class ServerOnly
