@file:HtmlComposable

package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.Updater
import net.derfruhling.serenity.annotations.HtmlComposable
import net.derfruhling.serenity.tree.HtmlApplier
import net.derfruhling.serenity.tree.platform.ElementNode
import net.derfruhling.serenity.tree.platform.TextNode

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Element(
    name: Name,
) {
    ComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
        set(name) { this.name = it }
    })
}

@Composable
inline fun Element(
    name: Name,
    content: @Composable () -> Unit
) {
    ComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
        set(name) { this.name = it }
    }, content)
}

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Element(
    name: String,
) = Element(Name.of(name))

@Composable
inline fun Element(
    name: String,
    content: @Composable () -> Unit
) = Element(Name.of(name), content = content)

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Element(
    update: @DisallowComposableCalls Updater<ElementNode>.() -> Unit,
    name: Name,
) {
    ComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
        set(name) { this.name = it }
        update()
    })
}

@Composable
inline fun Element(
    update: @DisallowComposableCalls Updater<ElementNode>.() -> Unit,
    name: Name,
    content: @Composable () -> Unit
) {
    ComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
        set(name) { this.name = it }
        update()
    }, content)
}

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Element(
    update: @DisallowComposableCalls Updater<ElementNode>.() -> Unit,
    name: String,
) = Element(update, Name.of(name))

@Composable
inline fun Element(
    update: @DisallowComposableCalls Updater<ElementNode>.() -> Unit,
    name: String,
    content: @Composable () -> Unit
) = Element(update, Name.of(name), content = content)

@Composable
fun Text(content: String) {
    ComposeNode<TextNode, HtmlApplier>(::TextNode, update = {
        set(content) { this.textContent = it }
    })
}
