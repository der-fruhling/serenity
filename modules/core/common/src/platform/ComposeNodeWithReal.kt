package net.derfruhling.serenity.platform

sealed class ComposeNodeWithReal<R : RealNode> : ComposeNode {
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