package net.derfruhling.serenity.compiler

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtExpression

object SerenityWarnings : KtDiagnosticsContainer() {
    val HELP_CONSTANT_NAME = KtDiagnosticFactory2<String, Long>(
        "HELP_CONSTANT_NAME",
        Severity.WARNING,
        SourceElementPositioningStrategies.DEFAULT,
        KtExpression::class,
        SerenityWarningMessages
    )

    override fun getRendererFactory(): BaseDiagnosticRendererFactory {
        return SerenityWarningMessages
    }
}

object SerenityWarningMessages : BaseDiagnosticRendererFactory() {
    override val MAP by KtDiagnosticFactoryToRendererMap("Serenity") {
        it.put(
            SerenityWarnings.HELP_CONSTANT_NAME,
            "{0} ::> {1}",
            KtDiagnosticRenderers.TO_STRING,
            KtDiagnosticRenderers.TO_STRING
        )
    }
}
