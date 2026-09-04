package net.derfruhling.serenity.platform

import net.derfruhling.serenity.Formatter

sealed interface ComposeNode {
    val index: Index<ComposeNode>

    fun realize() {}

    fun applied() {}

    fun removed() {}

    fun reuse() {}

    fun format(fmt: Formatter)
}

val ComposeNode.textContent: String
    get() = when (this) {
        is TextNode -> textContent
        is NodeWithChildren<*, *> -> children.mapNotNull { it.textContent.takeIf { s -> s.isNotBlank() } }
            .joinToString(" ")

        else -> ""
    }
