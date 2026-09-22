package net.derfruhling.serenity.compiler

import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtVariableDeclaration

object SerenityWarnings : KtDiagnosticsContainer() {
    val HELP_CONSTANT_NAME = KtDiagnosticFactory2<String, Long>(
        "HELP_CONSTANT_NAME",
        Severity.WARNING,
        SourceElementPositioningStrategies.DEFAULT,
        KtExpression::class,
        SerenityWarningMessages
    )

    val UNSTABLE_PROPERTY = KtDiagnosticFactory0(
        "UNSTABLE_PROPERTY",
        Severity.WARNING,
        SourceElementPositioningStrategies.DECLARATION_NAME,
        KtVariableDeclaration::class,
        SerenityWarningMessages
    )

    val MUTABLE_PROPERTY = KtDiagnosticFactory0(
        "UNSTABLE_PROPERTY",
        Severity.WARNING,
        SourceElementPositioningStrategies.DECLARATION_NAME,
        KtVariableDeclaration::class,
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

        it.put(
            SerenityWarnings.UNSTABLE_PROPERTY,
            "This property is in a stable class, but has a setter, and therefore may not be upholding it's contract. If the setter is notifying the runtime as @StableMarker describes, annotate the property with @NotifiesRuntime"
        )

        it.put(
            SerenityWarnings.MUTABLE_PROPERTY,
            "Properties in @Immutable classes must not have a setter"
        )
    }
}
