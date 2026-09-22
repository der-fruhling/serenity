@file:HtmlComposable

package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.Updater
import net.derfruhling.serenity.annotations.UnescapedTextDanger
import net.derfruhling.serenity.platform.*

internal val defaultFn = @Composable {}

@Composable
inline fun Element(
    name: Name,
    content: @Composable () -> Unit = {}
) {
    ReusableComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
        init(name) { this.name = it }
    }, content)
}

@Composable
inline fun Element(
    name: String,
    content: @Composable () -> Unit = {}
) {
    val name = Name.of(name)
    ReusableComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
        init(name) { this.name = it }
    }, content)
}

@Composable
inline fun Element(
    name: Name,
    update: @DisallowComposableCalls Updater<ElementNode>.() -> Unit,
    content: @Composable () -> Unit = {}
) {
    ReusableComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
        init(name) { this.name = it }
        update()
    }, content)
}

@Composable
inline fun Element(
    name: String,
    update: @DisallowComposableCalls Updater<ElementNode>.() -> Unit,
    content: @Composable () -> Unit = {}
) {
    val name = Name.of(name)
    ReusableComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
        init(name) { this.name = it }
        update()
    }, content)
}

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

@Composable
@UnescapedTextDanger
fun Data(content: String) {
    ReusableComposeNode<DataNode, HtmlApplier>(::DataNode, update = {
        set(content) { this.textContent = it }
    })
}

