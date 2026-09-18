package net.derfruhling.serenity.modularity.extension

abstract class ReferencePointSequence {
    abstract val points: List<ReferencePoint>
    abstract val default: ReferencePoint

    open fun comparePoints(a: ReferencePoint, b: ReferencePoint): Int {
        return points.indexOf(a).compareTo(points.indexOf(b))
    }
}