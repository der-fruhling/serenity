package net.derfruhling.serenity.compiler.fir

import net.derfruhling.serenity.compiler.Names
import net.derfruhling.serenity.compiler.SerenityWarnings
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassLikeChecker
import org.jetbrains.kotlin.fir.analysis.checkers.toClassLikeSymbol
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid

class StabilityCheckerExtension(session: FirSession) : FirAdditionalCheckersExtension(session) {
    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        override val classLikeCheckers: Set<FirClassLikeChecker> = setOf(StabilityChecker())
    }

    inner class StabilityChecker : FirClassLikeChecker(MppCheckerKind.Platform) {
        context(
            context: CheckerContext,
            reporter: DiagnosticReporter
        )
        override fun check(declaration: FirClassLikeDeclaration) {
            val applicableAnnotations = declaration.annotations.filter {
                it.annotationTypeRef.toClassLikeSymbol(session)!!.hasAnnotation(Names.androidxStableMarkerClass, session)
            }

            if(applicableAnnotations.isNotEmpty()) {
                val isImmutable = applicableAnnotations.any { it.annotationTypeRef.toClassLikeSymbol(session)!!.classId == Names.androidxImmutableClass }

                declaration.acceptChildren(object : FirVisitorVoid() {
                    override fun visitElement(element: FirElement) {}

                    override fun visitProperty(property: FirProperty) {
                        if(property.setter != null && (isImmutable || !property.hasAnnotation(Names.notifiedRuntimeClass, session))) {
                            reporter.reportOn(property.source, if(isImmutable) {
                                SerenityWarnings.MUTABLE_PROPERTY
                            } else SerenityWarnings.UNSTABLE_PROPERTY)
                        }
                    }
                })
            }
        }
    }
}
