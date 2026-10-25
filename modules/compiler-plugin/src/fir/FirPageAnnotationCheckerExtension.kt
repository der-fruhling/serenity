package net.derfruhling.serenity.compiler.fir

import net.derfruhling.serenity.compiler.Predicates.isPage
import net.derfruhling.serenity.compiler.SerenityErrors
import net.derfruhling.serenity.compiler.SerenityWarnings
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirNamedFunctionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.references.symbol
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassifierSymbol

class FirPageAnnotationCheckerExtension(session: FirSession) : FirAdditionalCheckersExtension(
    session
) {
    override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
        override val functionCallCheckers: Set<FirFunctionCallChecker> = setOf(
            object : FirFunctionCallChecker(MppCheckerKind.Common) {
                context(
                    context: CheckerContext,
                    reporter: DiagnosticReporter
                )
                override fun check(expression: FirFunctionCall) {
                    if(session.predicateBasedProvider.matches(isPage, expression.calleeReference.symbol!!)) {
                        reporter.reportOn(expression.source, SerenityWarnings.CALLED_PAGE_FUN)
                    }
                }
            }
        )
    }

    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        override val namedFunctionCheckers: Set<FirNamedFunctionChecker> = setOf(
            object : FirNamedFunctionChecker(MppCheckerKind.Platform) {
                context(
                    context: CheckerContext,
                    reporter: DiagnosticReporter
                )
                override fun check(declaration: FirNamedFunction) {
                    if(session.predicateBasedProvider.matches(isPage, declaration)) {
                        if (context.containingDeclarations.any { it is FirClassifierSymbol<*> }) {
                            reporter.reportOn(declaration.source, SerenityErrors.PAGE_MUST_BE_TOP_LEVEL)
                        } else {
                            val packageFqName = context.containingFileSymbol!!.packageFqName
                            val topLevel = session.symbolProvider.getTopLevelFunctionSymbols(packageFqName, declaration.name)
                                .filter { session.predicateBasedProvider.matches(isPage, it) }

                            if(topLevel.size > 1) {
                                reporter.reportOn(declaration.source, SerenityErrors.DUPLICATE_PAGE)
                            }
                        }
                    }
                }
            }
        )
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(isPage)
    }
}
