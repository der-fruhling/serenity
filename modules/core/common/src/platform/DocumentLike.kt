package net.derfruhling.serenity.platform

sealed class DocumentLike<This : NodeWithChildren<This, U>, U : RealElementLike> : NodeWithChildren<This, U> {
    constructor() : super()
    constructor(from: U) : super(from)

    abstract override fun shallowCopy(): This

    @Suppress("UNCHECKED_CAST")
    override fun deepCopy(): This {
        return super.deepCopy() as This
    }
}