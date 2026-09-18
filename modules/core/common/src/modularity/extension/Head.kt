package net.derfruhling.serenity.modularity.extension

object Head : ReferencePointSequence() {
    override val points: List<ReferencePoint>
        get() = listOf(Title, Content)
    override val default: ReferencePoint
        get() = Content

    object Title : ReferencePoint()
    object Content : ReferencePoint()
}
