package net.derfruhling.serenity.annotations

@RequiresOptIn("@Intrinsic has special meaning, using it in your own code is almost certainly an error")
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class IntrinsicHasSpecialMeaning

@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
@IntrinsicHasSpecialMeaning
annotation class Intrinsic(val role: Role)

@IntrinsicHasSpecialMeaning
enum class Role {
    PAGE_CONTRACT,
    PAGE_INIT,
    PAGE_ANNOTATION,
    PROMOTED_LAMBDA_ANNOTATION,
    LOCALIZATION_CONSTANT_NAME
}

/**
 * Denotes that a lambda passed as an argument will be "promoted" into a full
 * function at compile time. Parameters from the parent function will be usable
 * from within the lambda, but attempting to capture other local variables is
 * an error.
 */
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
@IntrinsicHasSpecialMeaning
@Intrinsic(Role.PROMOTED_LAMBDA_ANNOTATION)
annotation class PromotedLambda
