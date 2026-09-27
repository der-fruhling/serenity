package net.derfruhling.serenity.compiler

import org.jetbrains.kotlin.backend.common.extensions.DeclarationFinder
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.irAnnotation
import org.jetbrains.kotlin.ir.expressions.IrAnnotation
import org.jetbrains.kotlin.ir.expressions.impl.IrAnnotationImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.platform.NativePlatform
import org.jetbrains.kotlin.platform.PotentiallyWebPlatform
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.jvm.JvmPlatform

enum class Side(val annotationClass: ClassId) {
    CLIENT(Names.clientClass),
    SERVER(Names.serverClass);

    fun createAnnotation(finder: DeclarationFinder): IrAnnotation = IrAnnotationImpl(
        SYNTHETIC_OFFSET,
        SYNTHETIC_OFFSET,
        finder.findClass(Names.clientClass)!!.defaultType,
        finder.findConstructors(annotationClass).single(),
        0, 0
    )

    companion object {
        fun of(platform: TargetPlatform?): Side? {
            var side = null as Side?

            for (p in platform ?: return null) {
                when (p) {
                    is JvmPlatform, is NativePlatform -> {
                        if (side == CLIENT) return null
                        side = SERVER
                    }

                    is PotentiallyWebPlatform -> {
                        if (p.isWeb) {
                            if (side == SERVER) return null
                            side = CLIENT
                        } else {
                            if (side == CLIENT) return null
                            side = SERVER
                        }
                    }
                }
            }

            return side
        }
    }
}
