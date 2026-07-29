package net.derfruhling.serenity.manifest

import androidx.compose.runtime.staticCompositionLocalOf

data class Preload(val href: String, val `as`: String)

val preloadSetLocal = staticCompositionLocalOf<MutableSet<Preload>?> { null }
