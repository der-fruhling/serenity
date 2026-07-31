package net.derfruhling.serenity.tree

import androidx.compose.runtime.Recomposer

open class HtmlCompositionContext(val compositionContext: Recomposer) : AutoCloseable {
    var enableDebugMode = false

    override fun close() {
        compositionContext.close()
    }
}
