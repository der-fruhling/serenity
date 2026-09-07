package net.derfruhling.serenity.dynamic

import androidx.compose.runtime.Stable

@Stable
interface DynamicStringContext {
    val args: List<DynamicStringRenderable>
}
