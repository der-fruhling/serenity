package net.derfruhling.html.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposition
import io.github.oshai.kotlinlogging.KotlinLogging
import net.derfruhling.html.annotations.HtmlComposable
import net.derfruhling.html.tree.platform.DocumentFragment
import net.derfruhling.html.tree.platform.PlatformApplier

private val logger = KotlinLogging.logger {}

context(html: HtmlCompositionContext)
fun composeHtmlOnce(fn: @Composable @HtmlComposable () -> Unit): DocumentFragment {
    val tree = DocumentFragment()
    val applier = PlatformApplier(tree)

    logger.trace { "Begin compose: $fn" }
    val composition = ReusableComposition(applier, html.compositionContext)
    composition.setContentWithReuse(fn)
    composition.dispose()
    logger.trace { "Composition complete: $fn" }

    return tree
}
