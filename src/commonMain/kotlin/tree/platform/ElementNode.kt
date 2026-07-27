package net.derfruhling.html.tree.platform

import androidx.compose.runtime.ComposeNodeLifecycleCallback
import net.derfruhling.html.Formatter
import net.derfruhling.html.Name
import net.derfruhling.html.attribute.Attribute
import net.derfruhling.html.event.EventSubscriptionHandle
import net.derfruhling.html.event.EventType

open class ElementNode : NodeWithChildren<RealElement>, ChildNode<NodeWithChildren<*>>, ComposeNodeLifecycleCallback {
    override var parent: NodeWithChildren<*>? = null
    override val index: Index<ElementNode> = Index(this)

    override fun reparent(newParent: NodeWithChildren<*>) {
        parent = newParent
    }

    private lateinit var _name: Name

    var name: Name
        get() = _name
        set(value) {
            if(!isRealInitialized) {
                real = RealElement(value)
                _name = value
            } else if (value != _name) {
                throw UnsupportedOperationException("cannot set name of element after creation")
            }
        }

    override fun updateReal() {
        _name = real.name
        byUnderlyingItem[real.node] = this
        super.updateReal()
    }

    constructor() : super()

    constructor(from: RealElement) : super(from) {
        updateReal()
    }

    final override fun testAttribute() {}

    inline fun <reified T : Any> attribute(name: Attribute<T>): T? {
        return (attributeNode(name))?.value as T?
    }

    fun <T : Any> attributeNode(name: Attribute<T>): AttributeNode<*>? =
        this.children.find { it is AttributeNode<*> && it.parser == name } as AttributeNode<*>?

    fun <T : Any> attribute(name: Attribute<T>, value: T?) {
        this.insert(this.children.size, AttributeNode(name, value))
    }

    inline fun <T> subscribe(eventType: EventType<T>, crossinline fn: (T) -> Unit): EventSubscriptionHandle {
        return real.subscribe(eventType, fn)
    }

    override fun format(fmt: Formatter) {
        fmt.enter(Formatter.Begin.ELEMENT, "Element") {
            for(child in children) {
                child.format(fmt)
            }
        }
    }

    override fun onDeactivate() {
        byUnderlyingItem.remove(real.node)
    }

    override fun onRelease() {}

    override fun onReuse() {
        byUnderlyingItem[real.node] = this
    }

    override fun toString(): String {
        return "Element ${if(this::_name.isInitialized) _name else "<uninitialized>"}"
    }

    companion object {
        val byUnderlyingItem = mutableMapOf<UnderlyingElement, ElementNode>()

        fun tryGet(element: UnderlyingElement): ElementNode {
            return byUnderlyingItem[element] ?: ElementNode(RealElement(element))
        }
    }
}
