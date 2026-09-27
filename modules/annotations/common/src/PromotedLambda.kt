package net.derfruhling.serenity.annotations

/**
 * Denotes that a lambda passed as an argument will be "promoted" into a full
 * function at compile time. Parameters from the parent function will be usable
 * from within the lambda, but attempting to capture other local variables is
 * an error.
 */
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
@Intrinsic
annotation class PromotedLambda
