package net.derfruhling.serenity.modularity.extension

abstract class ReferencePoint {
    operator fun <T> invoke(fn: T) = PageExtensionPoint.Fn(this, fn)
}
