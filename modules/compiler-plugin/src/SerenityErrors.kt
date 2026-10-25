package net.derfruhling.serenity.compiler

import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtAnnotation
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunction

object SerenityErrors : KtDiagnosticsContainer() {
    val ILLEGAL_SIDE = KtDiagnosticFactory2<Side, Side>(
        "ILLEGAL_SIDE",
        Severity.ERROR,
        SourceElementPositioningStrategies.DEFAULT,
        KtExpression::class,
        SerenityErrorMessages
    )

    val DUPLICATE_ATTRIBUTE = KtDiagnosticFactory0(
        "DUPLICATE_ATTRIBUTE",
        Severity.ERROR,
        SourceElementPositioningStrategies.DEFAULT,
        KtAnnotation::class,
        SerenityErrorMessages
    )

    val MISMATCHED_ATTRIBUTE = KtDiagnosticFactory0(
        "MISMATCHED_ATTRIBUTE",
        Severity.ERROR,
        SourceElementPositioningStrategies.DEFAULT,
        KtAnnotation::class,
        SerenityErrorMessages
    )

    val PAGE_MUST_BE_TOP_LEVEL = KtDiagnosticFactory0(
        "PAGE_MUST_BE_TOP_LEVEL",
        Severity.ERROR,
        SourceElementPositioningStrategies.DECLARATION_NAME,
        KtFunction::class,
        SerenityErrorMessages
    )

    val DUPLICATE_PAGE = KtDiagnosticFactory0(
        "DUPLICATE_PAGE",
        Severity.ERROR,
        SourceElementPositioningStrategies.DECLARATION_NAME,
        KtFunction::class,
        SerenityErrorMessages
    )

    val MISPLACED_PAGE_CONTRACT = KtDiagnosticFactory1<String>(
        "MISPLACED_PAGE_CONTRACT",
        Severity.ERROR,
        SourceElementPositioningStrategies.DEFAULT,
        KtCallElement::class,
        SerenityErrorMessages
    )

    override fun getRendererFactory(): BaseDiagnosticRendererFactory {
        return SerenityErrorMessages
    }
}

object SerenityErrorMessages : BaseDiagnosticRendererFactory() {
    override val MAP by KtDiagnosticFactoryToRendererMap("Serenity") {
        it.put(
            SerenityErrors.ILLEGAL_SIDE,
            "Expression requires side {0} but this code could be executed on side {1}",
            KtDiagnosticRenderers.TO_STRING,
            KtDiagnosticRenderers.TO_STRING
        )

        it.put(SerenityErrors.DUPLICATE_ATTRIBUTE, "Duplicate side attribute")
        it.put(SerenityErrors.MISMATCHED_ATTRIBUTE, "Some parent has a different attribute already")
        it.put(SerenityErrors.PAGE_MUST_BE_TOP_LEVEL, "@Page functions must be top-level")
        it.put(SerenityErrors.DUPLICATE_PAGE, "@Page functions cannot have overloads (they must be uniquely named)")
        it.put(SerenityErrors.MISPLACED_PAGE_CONTRACT, "Function {0} cannot be placed here", KtDiagnosticRenderers.TO_STRING)
    }
}
