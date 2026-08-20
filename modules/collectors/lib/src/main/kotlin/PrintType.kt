package net.derfruhling.serenity.processor

import com.google.devtools.ksp.symbol.KSModifierListOwner
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Modifier
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

fun printModifiers(obj: KSModifierListOwner, noExpect: Boolean = false): String = buildString {
    for(m in obj.modifiers) {
        append(when(m) {
            Modifier.PUBLIC -> "public "
            Modifier.PRIVATE -> "private "
            Modifier.INTERNAL -> "internal "
            Modifier.PROTECTED -> "protected "
            Modifier.IN -> "in "
            Modifier.OUT -> "out "
            Modifier.OVERRIDE -> "override "
            Modifier.LATEINIT -> "lateinit "
            Modifier.ENUM -> "enum "
            Modifier.SEALED -> "sealed "
            Modifier.ANNOTATION -> "annotation "
            Modifier.DATA -> "data "
            Modifier.INNER -> "inner "
            Modifier.FUN -> "fun "
            Modifier.VALUE -> "value "
            Modifier.SUSPEND -> "suspend "
            Modifier.TAILREC -> "tailrec "
            Modifier.OPERATOR -> "operator "
            Modifier.INFIX -> "infix "
            Modifier.INLINE -> "inline "
            Modifier.EXTERNAL -> "external "
            Modifier.ABSTRACT -> "abstract "
            Modifier.FINAL -> "final "
            Modifier.OPEN -> "open "
            Modifier.CONST -> "const "
            Modifier.VARARG -> "vararg "
            Modifier.NOINLINE -> "noinline "
            Modifier.CROSSINLINE -> "crossinline "
            Modifier.REIFIED -> "reified "
            Modifier.EXPECT if !noExpect -> "expect "
            Modifier.ACTUAL -> "actual "
            else -> ""
        })
    }
}
