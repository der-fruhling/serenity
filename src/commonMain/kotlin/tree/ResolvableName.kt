package net.derfruhling.html.tree

import net.derfruhling.html.Name

expect abstract class ResolvableName() : Name {
    abstract override var namespaceUrl: String?
}
