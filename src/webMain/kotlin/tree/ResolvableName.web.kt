package net.derfruhling.html.tree

import net.derfruhling.html.Name
import web.dom.Node

actual abstract class ResolvableName actual constructor() : Name() {
    actual abstract override var namespaceUrl: String?

    fun resolve(node: Node): String? {
        if(namespaceUrl == null) {
            namespaceUrl = namespace?.let { node.lookupPrefix(it) }
        }

        return namespaceUrl
    }
}