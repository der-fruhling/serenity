package net.derfruhling.html.attribute

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.mutableStateSetOf
import net.derfruhling.html.annotations.HtmlComposable
import net.derfruhling.html.tree.HtmlApplier
import net.derfruhling.html.tree.platform.AttributeNode

open class StringSetComposableAttribute(attribute: Attribute<MutableSet<String>>)
    : AbstractComposableAttribute<MutableSet<String>>(attribute) {
    @PublishedApi
    internal val list: MutableSet<String> = mutableStateSetOf()

    @Composable
    @HtmlComposable
    inline operator fun invoke(crossinline fn: @DisallowComposableCalls MutableSet<String>.() -> Unit) {
        list.fn()

        ReusableComposeNode<AttributeNode<MutableSet<String>>, HtmlApplier>(::AttributeNode, update = {
            set(attribute) { parser = it }
            set(list) { this.value = it }
        })
    }

    @Composable
    @HtmlComposable
    operator fun invoke(vararg values: String) {
        invoke {
            clear()
            addAll(values)
        }
    }
}
