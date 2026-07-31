package net.derfruhling.serenity.tree.platform

import net.derfruhling.serenity.Formatter
import net.derfruhling.serenity.Name
import net.derfruhling.serenity.attribute.Attribute
import net.derfruhling.serenity.attribute.AttributeValue

@Deprecated("Avoid if possible")
class AttributeNode<T : Any> : ComposeNodeWithReal<RealAttribute>, ChildNode<ElementNode> {
    override var parent: ElementNode? = null

    override fun reparent(newParent: NodeWithChildren<*, *>) {
        require(newParent is ElementNode) { "Attributes only valid on elements" }
        parent = newParent
    }

    private lateinit var _name: Name
    private lateinit var _parser: Attribute<T>

    constructor() : super()

    constructor(from: RealAttribute) : super(from) {
        updateReal()
    }

    constructor(name: Attribute<T>) : this() {
        parser = name
    }

    constructor(name: Attribute<T>, value: T?) : this(name) {
        this.value = value
    }

    constructor(name: Name) : this() {
        this.name = name
    }

    constructor(name: Name, value: T?) : this(name) {
        this.value = value
    }

    override fun updateReal() {
        this._name = real.name
        @Suppress("UNCHECKED_CAST")
        this._parser = AttributeRegistry.derive(real) as Attribute<T>
    }

    var name: Name
        get() = _name
        set(value) {
            if (!isRealInitialized) {
                real = RealAttribute(value)
            } else {
                throw UnsupportedOperationException("cannot set name after initialization")
            }
        }

    var parser: Attribute<T>
        get() = _parser
        set(value) {
            if (this::_name.isInitialized) {
                require(value.name == this._name)
            } else {
                if (!this::_parser.isInitialized) {
                    name = value.name
                } else {
                    throw UnsupportedOperationException("cannot set name after initialization")
                }
            }

            _parser = value
        }

    var value: T?
        get() = real.value.let { parser.parser(it) }
        set(value) {
            real.value = value.let { AttributeValue.of(it) }
        }

    override val index: Index<AttributeNode<*>> = Index(this)

    override fun format(fmt: Formatter) {
        fmt.enter(Formatter.Begin.ATTRIBUTE) {
            write(if (this@AttributeNode::_name.isInitialized) name.toString() else "<uninitialized>")
            write(" = ")

            enter(Formatter.Begin.VALUE) {
                if (isRealInitialized) {
                    write(value.toString())
                } else {
                    write("<???>")
                }
            }
        }
    }
}
