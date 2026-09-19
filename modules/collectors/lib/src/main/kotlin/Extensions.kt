package net.derfruhling.serenity.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

fun KSFunctionDeclaration.getExtendingAnnotations(): List<Pair<KSAnnotation, KSType>> {
    return annotations.mapNotNull {
        it.annotationType.resolve().declaration.annotations.find { a ->
            a.annotationType.resolve().declaration.qualifiedName!!.asString() == "net.derfruhling.serenity.annotations.ExtendsWith"
        }?.let { a -> it to a.arguments[0].value as KSType }
    }.toList()
}

fun addPageExtensions(
    out: Appendable,
    resolver: Resolver,
    extendingAnnotations: List<Pair<KSAnnotation, KSType>>
) {
    out.appendLine("@Transient".prependIndent())
    out.appendLine(
        "override val extensions: Map<KClass<out Annotation>, AbstractPageExtension> = mapOf(".prependIndent(
            "    "
        )
    )

    for ((annotation, type) in extendingAnnotations) {
        val annotationName =
            annotation.annotationType.resolve().declaration.qualifiedName!!.asString()
        val type = printType(resolver.createKSTypeReferenceFromKSType(type))
        val arguments = annotation.arguments.joinToString {
            buildString {
                it.name?.let { n -> append(n.asString() + " = ") }
                fun printArgument(v: Any?): String {
                    return when (v) {
                        is Boolean, is Number -> v.toString()
                        is String -> "\"$v\""
                        is KSType -> v.declaration.qualifiedName!!.asString() + "::class"
                        is KSClassDeclaration -> v.qualifiedName!!.asString()
                        is Array<*> -> "arrayOf(${v.joinToString(transform = ::printArgument)})"
                        is Collection<*> -> "arrayOf(${v.joinToString(transform = ::printArgument)})"
                        else -> throw IllegalArgumentException("Unknown value $v")
                    }
                }

                append(printArgument(it.value))
            }
        }

        out.appendLine("        $annotationName::class to $type($arguments),")
    }

    out.appendLine("    )")
}
