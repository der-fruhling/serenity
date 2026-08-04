package net.derfruhling.serenity.processor

import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance

fun printType(typeReference: KSTypeReference): String = buildString {
    val element = typeReference.element
    if (element != null) {
        val type = typeReference.resolve()
        append(type.declaration.qualifiedName!!.asString())

        element.typeArguments.joinToString {
            when (it.variance) {
                Variance.STAR -> {
                    return@joinToString "*"
                }

                Variance.INVARIANT -> ""
                Variance.COVARIANT -> "in "
                Variance.CONTRAVARIANT -> "out "
            } + printType(it.type!!)
        }
    }
}