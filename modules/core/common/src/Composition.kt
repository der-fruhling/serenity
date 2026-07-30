@file:HtmlComposable

package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.Updater
import net.derfruhling.serenity.annotations.HtmlComposable
import net.derfruhling.serenity.tree.HtmlApplier
import net.derfruhling.serenity.tree.platform.DocumentTypeNode
import net.derfruhling.serenity.tree.platform.ElementNode
import net.derfruhling.serenity.tree.platform.TextNode

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Element(
    name: Name,
) {
    ReusableComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
        set(name) { this.name = it }
    })
}

@Composable
inline fun Element(
    name: Name,
    content: @Composable () -> Unit
) {
    ReusableComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
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
    ReusableComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
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
    ReusableComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
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
fun DocumentType() {
    ReusableComposeNode<DocumentTypeNode, HtmlApplier>(::DocumentTypeNode, update = {
        // safety: init is only called once
        @OptIn(DocumentTypeNode.RequiresRealizationCheck::class)
        init {
            type = "html"
        }
    })
}

@Composable
fun Text(content: String) {
    ReusableComposeNode<TextNode, HtmlApplier>(::TextNode, update = {
        set(content) { this.textContent = it }
    })
}
