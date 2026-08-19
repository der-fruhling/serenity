@file:Suppress("DEPRECATION")

package net.derfruhling.serenity.tree.platform

import net.derfruhling.serenity.Name
import net.derfruhling.serenity.event.EventSubscriptionHandle
import net.derfruhling.serenity.event.EventType

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

expect class RealData(node: UnderlyingData) : RealNode {
    override val node: UnderlyingData

    constructor()

    var textContent: String
}

expect class RealComment(node: UnderlyingComment) : RealNode {
    override val node: UnderlyingComment

    constructor()

    var commentContent: String
}

expect class RealDocumentType(node: UnderlyingDocType) : RealNode {
    override val node: UnderlyingDocType

    constructor(from: RealDocumentType)
    constructor(type: String, public: String = "", system: String = "")

    val type: String
    val public: String
    val system: String
}

expect sealed class RealElementLike : RealNode {
    abstract val attributeSet: MutableSet<RealAttribute>
    val children: MutableList<RealNode>
}

expect class RealElement(node: UnderlyingElement) : RealElementLike {
    constructor(name: Name)

    val name: Name

    override var node: UnderlyingElement
        internal set
    override val attributeSet: MutableSet<RealAttribute>

    inline fun <T> subscribe(
        type: EventType<T>,
        crossinline handler: (T) -> Unit
    ): EventSubscriptionHandle
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

    fun deepCopy(): RealDocumentFragment
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
