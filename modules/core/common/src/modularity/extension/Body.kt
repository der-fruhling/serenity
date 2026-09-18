package net.derfruhling.serenity.modularity.extension

object Body : ReferencePointSequence() {
    override val points: List<ReferencePoint> = listOf(Content)
    override val default: ReferencePoint
        get() = Content

    data object Content : ReferencePoint()
}
