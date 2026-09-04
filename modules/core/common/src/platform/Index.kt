package net.derfruhling.serenity.platform

import kotlin.properties.Delegates

class Index<out T : ComposeNode>(val refersTo: T) {
    internal var index: Int by Delegates.notNull()

    override fun toString(): String {
        return "OpaqueIndex(-> $refersTo)"
    }
}
