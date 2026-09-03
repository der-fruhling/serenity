package net.derfruhling.serenity.compiler.fir

import net.derfruhling.serenity.compiler.Side
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassLikeSymbol
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.impl.FirEmptyAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.extensions.FirTypeAttributeExtension
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.types.ConeAttribute
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.ClassId

val clientClass = ClassId.fromString("net/derfruhling/serenity/annotations/ClientOnly")
val serverClass = ClassId.fromString("net/derfruhling/serenity/annotations/ServerOnly")

class TypeSideAttributeApplier(session: FirSession) : FirTypeAttributeExtension(session) {
    private val clientConeClass by lazy { clientClass.defaultType(emptyList()) }
    private val serverConeClass by lazy { serverClass.defaultType(emptyList()) }

    override fun convertAttributeToAnnotation(attribute: ConeAttribute<*>): FirAnnotation? {
        return if(attribute is TypeSideAttribute) {
            buildAnnotation {
                annotationTypeRef = buildResolvedTypeRef {
                    coneType = when(attribute.side) {
                        Side.CLIENT -> clientConeClass
                        Side.SERVER -> serverConeClass
                    }
                }

                argumentMapping = FirEmptyAnnotationArgumentMapping
            }
        } else null
    }

    override fun extractAttributeFromAnnotation(annotation: FirAnnotation): ConeAttribute<*>? {
        val sym = annotation.toAnnotationClassLikeSymbol(session) ?: return null
        if(sym.hasAnnotation(clientClass, session)) return TypeSideAttribute(Side.CLIENT)
        if(sym.hasAnnotation(serverClass, session)) return TypeSideAttribute(Side.SERVER)
        return null
    }
}