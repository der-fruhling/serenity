package net.derfruhling.serenity.compiler.fir

import net.derfruhling.serenity.compiler.SerenityErrors
import net.derfruhling.serenity.compiler.Side
import net.derfruhling.serenity.compiler.clientClass
import net.derfruhling.serenity.compiler.serverClass
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirBasicDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirCallChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirPropertyAccessExpressionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.toClassLikeSymbol
import org.jetbrains.kotlin.fir.analysis.checkers.type.TypeCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.getTargetType
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.resolve.firClassLike
import org.jetbrains.kotlin.fir.resolve.getContainingClassSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.hasResolvedType
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.platform.NativePlatform
import org.jetbrains.kotlin.platform.PotentiallyWebPlatform
import org.jetbrains.kotlin.platform.jvm.JvmPlatform

class SidedAnnotationCheckerExtension(session: FirSession) : FirAdditionalCheckersExtension(session) {
    val expectedSide by lazy {
        var side = null as Side?

        for (p in session.moduleData.platform) {
            when (p) {
                is JvmPlatform, is NativePlatform -> {
                    if (side == Side.CLIENT) return@lazy null
                    side = Side.SERVER
                }

                is PotentiallyWebPlatform -> {
                    if (p.isWeb) {
                        if (side == Side.SERVER) return@lazy null
                        side = Side.CLIENT
                    } else {
                        if (side == Side.CLIENT) return@lazy null
                        side = Side.SERVER
                    }
                }
            }
        }

        side
    }

    private fun FirAnnotation.getSide(): Side? {
        val classLike = annotationTypeRef.firClassLike(session) ?: return null
        getSideSimple(classLike)?.let { return it }
        return classLike.annotations.firstNotNullOfOrNull { it.getSideSimple() }
    }

    private fun FirAnnotation.getSideSimple(classLike: FirClassLikeDeclaration? = annotationTypeRef.firClassLike(session)): Side? {
        return when (classLike?.symbol?.classId) {
            clientClass -> Side.CLIENT
            serverClass -> Side.SERVER
            else -> null
        }
    }

    private tailrec fun <D> FirBasedSymbol<D>.getSidedAnnotations(): List<Pair<Side, FirAnnotation>> where D : FirAnnotationContainer, D : FirDeclaration {
        val list = resolvedAnnotationsWithClassIds.mapNotNull {
            it.getSide()?.let { side -> side to it }
        }

        if (list.isNotEmpty()) return list
        val container = getContainingClassSymbol() ?: return list
        return container.getSidedAnnotations()
    }

    context(
        context: CheckerContext,
        reporter: DiagnosticReporter
    )
    private fun <D> checkAttrs(element: FirElement, symbol: FirBasedSymbol<D>, expectedSide: Side = this.expectedSide!!) where D : FirAnnotationContainer, D : FirDeclaration {
        val attrs = symbol.getSidedAnnotations()
        when {
            attrs.isEmpty() -> return
            attrs.size > 1 -> {
                for((_, annotation) in attrs.listIterator(1)) {
                    reporter.reportOn(annotation.source, SerenityErrors.DUPLICATE_ATTRIBUTE)
                }
            }
        }

        val (side, _) = attrs.first()
        if (side != expectedSide) reporter.reportOn(element.source, SerenityErrors.ILLEGAL_SIDE, side, expectedSide)
    }

    context(
        context: CheckerContext,
        reporter: DiagnosticReporter
    )
    private tailrec fun checkConsistency(side: Side, annotation: FirElement, container: FirBasedSymbol<*>) {
        val attrs = container.getSidedAnnotations().firstOrNull()

        if(attrs != null && attrs.first != side) {
            reporter.reportOn(annotation.source, SerenityErrors.MISMATCHED_ATTRIBUTE)
            return
        }

        val parent = container.getContainingClassSymbol()
        if(parent != null) checkConsistency(side, annotation, parent)
    }

    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        override val basicDeclarationCheckers: Set<FirBasicDeclarationChecker> = setOf(
            object : FirBasicDeclarationChecker(MppCheckerKind.Common) {
                context(
                    context: CheckerContext,
                    reporter: DiagnosticReporter
                )
                override fun check(declaration: FirDeclaration) {
                    val attrs = declaration.symbol.getSidedAnnotations()
                    when {
                        attrs.isEmpty() -> return
                        attrs.size > 1 -> {
                            for((_, annotation) in attrs.listIterator(1)) {
                                reporter.reportOn(annotation.source, SerenityErrors.DUPLICATE_ATTRIBUTE)
                            }
                        }
                    }

                    declaration.getContainingClassSymbol()?.let { container ->
                        val (side, annotation) = attrs.first()
                        checkConsistency(side, annotation, container)
                    }
                }
            }
        )
    }

    override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
        override val callCheckers: Set<FirCallChecker> = setOf(
            object : FirCallChecker(MppCheckerKind.Platform) {
                context(
                    context: CheckerContext,
                    reporter: DiagnosticReporter
                )
                override fun check(expression: FirCall) {
                    val contextSide = context.annotationContainers.firstNotNullOfOrNull {
                        it.annotations.firstNotNullOfOrNull { a -> a.getSide() }
                    } ?: expectedSide ?: return

                    if(expression is FirExpression && expression !is FirAnnotationCall) {
                        expression.toResolvedCallableSymbol(session)?.let { sym ->
                            checkAttrs(expression, sym, contextSide)
                        }
                    }
                }
            }
        )

        override val propertyAccessExpressionCheckers: Set<FirPropertyAccessExpressionChecker> = setOf(
            object : FirPropertyAccessExpressionChecker(MppCheckerKind.Platform) {
                context(
                    context: CheckerContext,
                    reporter: DiagnosticReporter
                )
                override fun check(expression: FirPropertyAccessExpression) {
                    val contextSide = context.annotationContainers.firstNotNullOfOrNull {
                        it.annotations.firstNotNullOfOrNull { a -> a.getSide() }
                    } ?: expectedSide ?: return

                    expression.toResolvedCallableSymbol()?.let {
                        checkAttrs(expression, it, contextSide)
                    }
                }
            }
        )
    }

    override val typeCheckers: TypeCheckers
        get() = super.typeCheckers
}
