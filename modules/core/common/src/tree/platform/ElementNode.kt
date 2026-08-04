package net.derfruhling.serenity.tree.platform

import androidx.compose.runtime.ComposeNodeLifecycleCallback
import net.derfruhling.serenity.Formatter
import net.derfruhling.serenity.Name
import net.derfruhling.serenity.attribute.Attribute
import net.derfruhling.serenity.attribute.AttributeValue
import net.derfruhling.serenity.event.EventSubscriptionHandle
import net.derfruhling.serenity.event.EventTarget
import net.derfruhling.serenity.event.EventType

open class ElementNode : NodeWithChildren<ElementNode, RealElement>,
                         ChildNode<NodeWithChildren<*, *>>,
                         ComposeNodeLifecycleCallback {
    override var parent: NodeWithChildren<*, *>? = null
    override val index: Index<ElementNode> = Index(this)

    override fun reparent(newParent: NodeWithChildren<*, *>) {
        parent = newParent
    }

    private lateinit var _name: Name

    var name: Name
        get() = _name
        set(value) {
            if (!isRealInitialized) {
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

    override fun existingFrom(child: RealNode): ChildNode<in ElementNode>? {
        return when (child) {
            is RealText -> TextNode(child)
            else -> simpleExisting(child)
        }
    }

    constructor() : super()

    constructor(from: RealElement) : super(from) {
        updateReal()
    }

    constructor(name: Name) : super(RealElement(name))
    constructor(name: String) : this(Name.of(name))

    val classes: MutableSet<String> = object : AbstractMutableSet<String>() {
        private val name = Name.of("class")
        private var value: String
            get() = real.attributeSet.find { it.name == name }?.value ?: ""
            set(value) {
                real.attributeSet.add(RealAttribute(name, value))
            }

        private var contents: Set<String>
            get() = value.split(' ').filter { it.isNotBlank() }.toSet()
            set(value) {
                this.value = value.joinToString(" ")
            }

        override val size: Int
            get() = contents.size

        override fun iterator(): MutableIterator<String> {
            return object : MutableIterator<String> {
                private val contentSet = contents.asIterable().toMutableSet()
                private val iterator = contentSet.iterator()

                override fun remove() {
                    iterator.remove()
                    contents = contentSet
                }

                override fun next(): String {
                    return iterator.next()
                }

                override fun hasNext(): Boolean {
                    return iterator.hasNext()
                }
            }
        }

        override fun add(element: String): Boolean {
            if (element in contents) {
                return false
            } else {
                contents += element
                return true
            }
        }
    }

    interface ClassMapKey

    val classMap: MutableMap<ClassMapKey, String?> =
        object : AbstractMutableMap<ClassMapKey, String?>() {
            inner class Entry(
                override val key: ClassMapKey,
                value: String?
            ) : MutableMap.MutableEntry<ClassMapKey, String?> {
                init {
                    value?.let { classes.add(value) }
                }

                override var value = value
                    private set

                override fun setValue(newValue: String?): String? {
                    value?.let { classes.remove(it) }
                    newValue?.let { classes.add(newValue) }
                    return value.also { value = newValue }
                }
            }

            private val _entries = mutableMapOf<ClassMapKey, Entry>()

            override val entries: MutableSet<MutableMap.MutableEntry<ClassMapKey, String?>> =
                object : AbstractMutableSet<MutableMap.MutableEntry<ClassMapKey, String?>>() {
                    override fun add(element: MutableMap.MutableEntry<ClassMapKey, String?>): Boolean {
                        return put(element.key, element.value) != element.value
                    }

                    override fun iterator(): MutableIterator<MutableMap.MutableEntry<ClassMapKey, String?>> {
                        return object : MutableIterator<MutableMap.MutableEntry<ClassMapKey, String?>> {
                            private var iterator = _entries.values.iterator()
                            private lateinit var current: Entry

                            override fun remove() {
                                classes.remove(current.value)
                                iterator.remove()
                            }

                            override fun hasNext(): Boolean {
                                return iterator.hasNext()
                            }

                            override fun next(): MutableMap.MutableEntry<ClassMapKey, String?> {
                                return iterator.next().also { current = it }
                            }
                        }
                    }

                    override val size: Int
                        get() = _entries.size
                }

            override fun put(
                key: ClassMapKey,
                value: String?
            ): String? {
                return _entries[key]?.setValue(value) ?: run {
                    _entries[key] = Entry(key, value)
                    null
                }
            }
        }

    fun setClass(name: String, enabled: Boolean) {
        if (enabled) {
            classes.add(name)
        } else {
            classes.remove(name)
        }
    }

    final override fun testAttribute() {}

    inline fun <reified T : Any> attribute(name: Attribute<T>): T? {
        return real.attributeSet.find { it.name == name.name }?.let { name.parser(it.value) }
    }

    @Deprecated("Avoid if possible")
    fun <T : Any> attributeNode(name: Attribute<T>): AttributeNode<*>? =
        this.children.find { it is AttributeNode<*> && it.parser == name } as AttributeNode<*>?

    fun <T : Any> attribute(name: Attribute<T>, value: T?) {
        real.attributeSet.add(RealAttribute(name.name, AttributeValue.of(value)))
    }

    fun attribute(name: Attribute<Boolean>, value: Boolean) {
        if (value) {
            real.attributeSet.add(RealAttribute(name.name, null))
        } else {
            real.attributeSet.removeAll { it.name == name.name }
        }
    }

    inline fun <T> subscribe(
        eventType: EventType<T>,
        crossinline fn: (T) -> Unit
    ): EventSubscriptionHandle {
        return real.subscribe(eventType, fn)
    }

    override fun format(fmt: Formatter) {
        fmt.enter(Formatter.Begin.ELEMENT, "Element") {
            for (child in children) {
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
        return "Element ${if (this::_name.isInitialized) _name else "<uninitialized>"}"
    }

    companion object {
        val byUnderlyingItem = mutableMapOf<UnderlyingElement, ElementNode>()

        fun tryGet(element: UnderlyingElement): ElementNode {
            return byUnderlyingItem[element] ?: ElementNode(RealElement(element))
        }
    }
}
