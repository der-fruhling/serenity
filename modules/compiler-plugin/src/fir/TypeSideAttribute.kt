package net.derfruhling.serenity.compiler.fir

import net.derfruhling.serenity.compiler.Side
import org.jetbrains.kotlin.fir.types.ConeAttribute
import org.jetbrains.kotlin.fir.types.ConeAttributes
import kotlin.reflect.KClass

class TypeSideAttribute(val side: Side) : ConeAttribute<TypeSideAttribute>() {
    override fun add(other: TypeSideAttribute?): TypeSideAttribute? {
        return null
    }

    override fun intersect(other: TypeSideAttribute?): TypeSideAttribute? {
        return null
    }

    override fun isSubtypeOf(other: TypeSideAttribute?): Boolean {
        return other != null
    }

    override fun toString(): String = when (side) {
        Side.CLIENT -> "[client]"
        Side.SERVER -> "[server]"
    }

    override fun union(other: TypeSideAttribute?): TypeSideAttribute? {
        return null
    }

    override val keepInInferredDeclarationType: Boolean
        get() = true
    override val key: KClass<out TypeSideAttribute>
        get() = TypeSideAttribute::class
}

val ConeAttributes.typeSideAttribute: TypeSideAttribute? by ConeAttributes.attributeAccessor()
