package net.derfruhling.html.tree

import net.derfruhling.html.Name

actual abstract class ResolvableName actual constructor() : Name() {
    actual abstract override var namespaceUrl: String?

    // still TODO
}