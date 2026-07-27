package net.derfruhling.html.tree.platform

import androidx.compose.runtime.Composable
import net.derfruhling.html.Name
import net.derfruhling.html.event.EventSubscriptionHandle
import net.derfruhling.html.event.EventType

interface RealNode {
    val node: UnderlyingBase
}

expect object RealUnknown : RealNode {
    override val node: UnderlyingBase
}

expect fun RealNode(base: UnderlyingBase): RealNode?

expect class RealText(node: UnderlyingText) : RealNode {
    override val node: UnderlyingText

    constructor()

    var textContent: String
}

expect class RealComment(node: UnderlyingComment) : RealNode {
    override val node: UnderlyingComment

    constructor()

    var commentContent: String
}

expect sealed class RealElementLike : RealNode {
    abstract val attributeSet: MutableSet<RealAttribute>
    val children: MutableList<RealNode>
}

expect class RealElement(node: UnderlyingElement) : RealElementLike {
    constructor(name: Name)

    val name: Name

    override val node: UnderlyingElement
    override val attributeSet: MutableSet<RealAttribute>

    inline fun <T> subscribe(type: EventType<T>, crossinline handler: (T) -> Unit): EventSubscriptionHandle
}

expect class RealDocument(node: UnderlyingDocument) : RealElementLike {
    override val node: UnderlyingDocument
    @Deprecated("Attributes not supported on document objects", level = DeprecationLevel.ERROR)
    override val attributeSet: MutableSet<RealAttribute>

    fun applyFragment(realDocumentFragment: RealDocumentFragment)
}

expect class RealDocumentFragment(node: UnderlyingDocumentFragment) : RealElementLike {
    override val node: UnderlyingDocumentFragment
    @Deprecated("Attributes not supported on document objects", level = DeprecationLevel.ERROR)
    override val attributeSet: MutableSet<RealAttribute>

    constructor()
}

object RealElementNamer : Registry.Namer<RealElement> {
    override fun nameOf(value: RealElement): Name {
        return value.name
    }
}

expect class RealAttribute(node: UnderlyingAttribute) : RealNode {
    constructor(name: Name)
    constructor(name: Name, value: String?)

    override val node: UnderlyingAttribute
    val name: Name
    var value: String?
}

object RealAttributeNamer : Registry.Namer<RealAttribute> {
    override fun nameOf(value: RealAttribute): Name {
        return value.name
    }
}
