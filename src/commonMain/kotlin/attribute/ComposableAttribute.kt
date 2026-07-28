package net.derfruhling.serenity.attribute

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.remember
import net.derfruhling.serenity.annotations.HtmlComposable
import net.derfruhling.serenity.tree.HtmlApplier
import net.derfruhling.serenity.tree.platform.AttributeNode

open class ComposableAttribute<T : Any>(attribute: Attribute<T>) : AbstractComposableAttribute<T>(attribute) {
    @Composable
    @HtmlComposable
    inline operator fun invoke(crossinline fn: @DisallowComposableCalls () -> T) {
        val value = remember { fn() }
        ReusableComposeNode<AttributeNode<T>, HtmlApplier>(::AttributeNode, update = {
            set(attribute) { parser = it }
            set(value) { this.value = it }
        })
    }
}

