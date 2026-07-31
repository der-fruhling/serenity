package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.currentComposer
import net.derfruhling.serenity.annotations.HtmlComposable
import net.derfruhling.serenity.tree.HtmlApplier

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
    CompositionLocalProvider(reflowEnabled provides false, fn)
}

@Composable
fun reflow(fn: @Composable () -> Unit) {
    CompositionLocalProvider(reflowEnabled provides true, fn)
}
