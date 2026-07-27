package net.derfruhling.html

import net.derfruhling.html.tree.ResolvableName

abstract class Name {
    abstract val namespaceUrl: String?
    abstract val namespace: String?
    abstract val localName: String

    private class Simple(override val localName: String, namespaceUrl: String? = null) : ResolvableName() {
        override val namespace: String? get() = null
        override var namespaceUrl: String? = namespaceUrl ?: HTML_NS

        override fun toString(): String {
            return localName
        }
    }

    private class Qualified(
        override val namespace: String,
        namespaceUrl: String?,
        override val localName: String
    ) : ResolvableName() {
        override var namespaceUrl: String? = namespaceUrl ?: HTML_NS

        override fun toString(): String {
            return "$namespace($namespaceUrl):$localName"
        }
    }

    companion object {
        val HTML_NS: String = "http://www.w3.org/1999/xhtml"

        fun of(name: String): Name {
            return Simple(name)
        }

        fun of(namespaceUrl: String, name: String): Name {
            return Simple(name, namespaceUrl)
        }

        fun qualified(namespace: String, namespaceUrl: String, name: String): Name {
            return Qualified(namespace, namespaceUrl, name)
        }

        fun qualified(namespace: String, name: String): Name {
            return Qualified(namespace, null, name)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Name) return false

        if (namespaceUrl != other.namespaceUrl) return false
        if (namespace != other.namespace) return false
        if (localName != other.localName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = namespaceUrl.hashCode()
        result = 31 * result + namespace.hashCode()
        result = 31 * result + localName.hashCode()
        return result
    }
}


