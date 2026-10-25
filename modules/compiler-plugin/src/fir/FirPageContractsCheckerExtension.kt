package net.derfruhling.serenity.compiler.fir

import net.derfruhling.serenity.compiler.Predicates.isIntrinsic
import net.derfruhling.serenity.compiler.Predicates.isPage
import net.derfruhling.serenity.compiler.SerenityErrors
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirNamedFunctionChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.Name

class FirPageContractsCheckerExtension(session: FirSession) : FirAdditionalCheckersExtension(session) {
    private enum class Stage {
        PAGE_INIT,
        PAGE_CONTRACT,
        BODY,
    }

    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        override val namedFunctionCheckers: Set<FirNamedFunctionChecker> = setOf(
            object : FirNamedFunctionChecker(MppCheckerKind.Platform) {
                context(
                    context: CheckerContext,
                    reporter: DiagnosticReporter
                )
                override fun check(declaration: FirNamedFunction) {
                    if(!session.predicateBasedProvider.matches(isPage, declaration)) return

                    declaration.body?.acceptChildren(object : FirVisitorVoid() {
                        var stage = Stage.PAGE_INIT

                        override fun visitElement(element: FirElement) {}

                        override fun visitFunctionCall(functionCall: FirFunctionCall) {
                            val callableSymbol = functionCall.toResolvedCallableSymbol() ?: return
                            val intrinsic = session.predicateBasedProvider.matches(isIntrinsic,
                                callableSymbol
                            )

                            if(intrinsic) {
                                val expectedStage = when(callableSymbol.name.identifierOrNullIfSpecial) {
                                    "pageContract" -> Stage.PAGE_CONTRACT
                                    "pageInit",
                                    "serverInit",
                                    "clientInit" -> Stage.PAGE_INIT
                                    else -> Stage.BODY
                                }

                                if(expectedStage < stage) {
                                    reporter.reportOn(functionCall.source, SerenityErrors.MISPLACED_PAGE_CONTRACT, callableSymbol.name.asString())
                                }

                                stage = maxOf(expectedStage, stage)
                            } else {
                                stage = Stage.BODY
                            }

                            super.visitFunctionCall(functionCall)
                        }
                    })
                }
            }
        )
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(isIntrinsic, isPage)
    }
}