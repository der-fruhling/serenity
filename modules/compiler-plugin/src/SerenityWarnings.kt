package net.derfruhling.serenity.compiler

import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtVariableDeclaration

object SerenityWarnings : KtDiagnosticsContainer() {
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

    val CALLED_PAGE_FUN = KtDiagnosticFactory0(
        "CALLED_PAGE_FUN",
        Severity.WARNING,
        SourceElementPositioningStrategies.DEFAULT,
        KtCallElement::class,
        SerenityWarningMessages
    )

    override fun getRendererFactory(): BaseDiagnosticRendererFactory {
        return SerenityWarningMessages
    }
}

object SerenityWarningMessages : BaseDiagnosticRendererFactory() {
    override val MAP by KtDiagnosticFactoryToRendererMap("Serenity") {
        it.put(
            SerenityWarnings.UNSTABLE_PROPERTY,
            "This property is in a stable class, but has a setter, and therefore may not be upholding it's contract. If the setter is notifying the runtime as @StableMarker describes, annotate the property with @NotifiesRuntime"
        )

        it.put(
            SerenityWarnings.MUTABLE_PROPERTY,
            "Properties in @Immutable classes must not have a setter"
        )

        it.put(
            SerenityWarnings.CALLED_PAGE_FUN,
            "@Page functions shouldn't be called as this will ignore a lot of internal machinery"
        )
    }
}
