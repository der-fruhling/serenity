package net.derfruhling.serenity.compiler

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiExpression
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory

object SerenityErrors : KtDiagnosticsContainer() {
    val ILLEGAL_SIDE = KtDiagnosticFactory2<Side, Side>(
        "ILLEGAL_SIDE",
        Severity.ERROR,
        SourceElementPositioningStrategies.DEFAULT,
        PsiExpression::class,
        SerenityErrorMessages
    )

    val DUPLICATE_ATTRIBUTE = KtDiagnosticFactory0(
        "DUPLICATE_ATTRIBUTE",
        Severity.ERROR,
        SourceElementPositioningStrategies.DEFAULT,
        PsiAnnotation::class,
        SerenityErrorMessages
    )


    val MISMATCHED_ATTRIBUTE = KtDiagnosticFactory0(
        "MISMATCHED_ATTRIBUTE",
        Severity.ERROR,
        SourceElementPositioningStrategies.DEFAULT,
        PsiAnnotation::class,
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
    }
}
