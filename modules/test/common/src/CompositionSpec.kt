package net.derfruhling.serenity.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.withRunningRecomposer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import net.derfruhling.serenity.tree.HtmlCompositionContext
import net.derfruhling.serenity.tree.composeHtmlOnce
import net.derfruhling.serenity.tree.platform.DocumentFragment

suspend fun runStaticComposeTest(
    fn: @Composable () -> Unit
): DocumentFragment {
    return withFrameClock {
        val recomposer = Recomposer(currentCoroutineContext())

        try {
            val fragment = context(HtmlCompositionContext(recomposer)) {
                composeHtmlOnce { fn() }
            }

            fragment
        } finally {
            recomposer.close()
            recomposer.join()
        }
    }
}

suspend fun runComposeTest(
    fn: @Composable () -> Unit,
    after: (DocumentFragment) -> Unit = {}
) {
    withFrameClock {
        val fragment = withRunningRecomposer {
            context(HtmlCompositionContext(it)) {
                composeHtmlOnce { fn() }
            }
        }

        after(fragment)
    }
}

internal expect suspend inline fun <T> withFrameClock(crossinline fn: suspend CoroutineScope.() -> T): T
