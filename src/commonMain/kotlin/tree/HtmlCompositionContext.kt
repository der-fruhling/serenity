package net.derfruhling.html.tree

import androidx.compose.runtime.Recomposer
import kotlin.coroutines.CoroutineContext

open class HtmlCompositionContext(val compositionContext: Recomposer) : AutoCloseable {
    var enableDebugMode = false

    override fun close() {
        compositionContext.close()
    }
}
