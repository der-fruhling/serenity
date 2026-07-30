package net.derfruhling.serenity.tree.platform

import net.derfruhling.serenity.Formatter

sealed interface ComposeNode {
    val index: Index<ComposeNode>

    fun realize() {}

    fun applied() {}

    fun removed() {}

    fun reuse() {}

    fun format(fmt: Formatter)
}

sealed class ComposeNodeWithReal<R: RealNode> : ComposeNode {
    private lateinit var _real: R

    val dom: UnderlyingBase
        get() = real.node

    protected val isRealInitialized: Boolean
        get() = this::_real.isInitialized

    var real: R
        get() = _real
        internal set(value) {
            _real = value
            updateReal()
        }

    protected open fun updateReal() {}

    constructor()

    constructor(from: R) : this() {
        this._real = from
    }
}

val ComposeNode.textContent: String
    get() = when(this) {
        is TextNode -> textContent
        is NodeWithChildren<*, *> -> children.mapNotNull { it.textContent.takeIf { s -> s.isNotBlank() } }.joinToString(" ")
        else -> ""
    }
