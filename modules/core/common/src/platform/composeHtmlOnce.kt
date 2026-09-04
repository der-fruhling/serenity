package net.derfruhling.serenity.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposition
import io.github.oshai.kotlinlogging.KotlinLogging
import net.derfruhling.serenity.HtmlComposable
import net.derfruhling.serenity.tree.HtmlCompositionContext

private val logger = KotlinLogging.logger {}

context(html: HtmlCompositionContext)
fun composeHtmlOnce(fn: @Composable @HtmlComposable () -> Unit): DocumentFragment {
    val tree = DocumentFragment()
    val applier = PlatformApplier(tree)

    logger.trace { "Begin compose: $fn" }
    val composition = ReusableComposition(applier, html.compositionContext)
    try {
        composition.setContentWithReuse(fn)
        logger.trace { "Composition complete: $fn" }
        return tree.deepCopy()
    } finally {
        composition.dispose()
    }
}
