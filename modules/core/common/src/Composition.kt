@file:HtmlComposable

package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.Updater
import net.derfruhling.serenity.annotations.UnescapedTextDanger
import net.derfruhling.serenity.attribute.Attribute
import net.derfruhling.serenity.tree.HtmlApplier
import net.derfruhling.serenity.tree.platform.DataNode
import net.derfruhling.serenity.tree.platform.DocumentTypeNode
import net.derfruhling.serenity.tree.platform.ElementNode
import net.derfruhling.serenity.tree.platform.TextNode

internal val defaultFn = @Composable {}

@Composable
fun Element(
    name: Name,
    content: @Composable () -> Unit = defaultFn
) {
    ReusableComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
        init(name) { this.name = it }
    }, content)
}

@Composable
fun Element(
    name: String,
    content: @Composable () -> Unit = defaultFn
) {
    val name = Name.of(name)
    ReusableComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
        init(name) { this.name = it }
    }, content)
}

@Composable
fun Element(
    update: @DisallowComposableCalls Updater<ElementNode>.() -> Unit,
    name: Name,
    content: @Composable () -> Unit = defaultFn
) {
    ReusableComposeNode<ElementNode, HtmlApplier>(::ElementNode, update = {
        init(name) { this.name = it }
        update()
    }, content)
}

@Composable
fun Element(
    update: @DisallowComposableCalls Updater<ElementNode>.() -> Unit,
    name: String,
    content: @Composable () -> Unit = defaultFn
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

fun <T : Any> Updater<ElementNode>.attribute(attribute: Attribute<T>, value: T?) {
    set(value) { attribute(attribute, it) }
}

fun Updater<ElementNode>.attribute(attribute: Attribute<Boolean>, value: Boolean) {
    set(value) { attribute(attribute, it) }
}
