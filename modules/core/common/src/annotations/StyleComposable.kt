package net.derfruhling.serenity.annotations

import androidx.compose.runtime.ComposableTargetMarker

@Target(
    AnnotationTarget.FILE,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.TYPE,
    AnnotationTarget.TYPE_PARAMETER,
)
@Retention(AnnotationRetention.BINARY)
@ComposableTargetMarker("Style")
annotation class StyleComposable
