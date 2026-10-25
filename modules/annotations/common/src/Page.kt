@file:OptIn(IntrinsicHasSpecialMeaning::class)

package net.derfruhling.serenity.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
@Intrinsic(Role.PAGE_ANNOTATION)
annotation class Page(
    val path: String
)
