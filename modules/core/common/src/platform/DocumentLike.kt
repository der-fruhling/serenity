package net.derfruhling.serenity.platform

sealed class DocumentLike<This : NodeWithChildren<This, U>, U : RealElementLike> : NodeWithChildren<This, U> {
    constructor() : super()
    constructor(from: U) : super(from)
}