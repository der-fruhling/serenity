package net.derfruhling.html.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.withCompositionLocal
import net.derfruhling.html.tree.HtmlApplier
import net.derfruhling.html.annotations.HtmlComposable

@PublishedApi
internal val reflowEnabled = compositionLocalOf { true }

val String.reflow: String
    @Composable
    @HtmlComposable
    get() = trimIndent().let {
        when (reflowEnabled.current) {
            true -> (currentComposer.applier as HtmlApplier).reflowTransformer?.invoke(it) ?: it
            false -> it
        }
    }

@Composable
fun cram(fn: @Composable () -> Unit) {
    withCompositionLocal(reflowEnabled provides false, fn)
}

@Composable
fun reflow(fn: @Composable () -> Unit) {
    withCompositionLocal(reflowEnabled provides true, fn)
}
