package net.derfruhling.html.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposition
import co.touchlab.kermit.Logger
import net.derfruhling.html.SerialRegistry
import net.derfruhling.html.SerialSavedData
import net.derfruhling.html.annotations.HtmlComposable
import net.derfruhling.html.tree.platform.DocumentFragment
import net.derfruhling.html.tree.platform.PlatformApplier

private val logger = Logger.withTag("net.derfruhling.html.tree.HtmlTreeCompositionKt")

context(html: HtmlCompositionContext)
fun composeHtmlOnce(fn: @Composable @HtmlComposable () -> Unit): DocumentFragment {
    val tree = DocumentFragment()
    val applier = PlatformApplier(tree)

    logger.v { "Begin compose: $fn" }
    val composition = ReusableComposition(applier, html.compositionContext)
    composition.setContentWithReuse(fn)
    composition.dispose()
    logger.v { "Composition complete: $fn" }

    return tree
}
