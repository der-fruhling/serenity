package net.derfruhling.serenity.compiler

import net.derfruhling.serenity.compiler.fir.FirPageAnnotationCheckerExtension
import net.derfruhling.serenity.compiler.fir.FirPageContractsCheckerExtension
import net.derfruhling.serenity.compiler.fir.FirPageDeclarationGenerator
import net.derfruhling.serenity.compiler.fir.FirSidedAnnotationCheckerExtension
import net.derfruhling.serenity.compiler.fir.FirStabilityCheckerExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class SerenityPluginRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::FirPageDeclarationGenerator
        +::FirPageAnnotationCheckerExtension
        +::FirPageContractsCheckerExtension
        +::FirSidedAnnotationCheckerExtension
        +::FirStabilityCheckerExtension

        registerDiagnosticContainers(SerenityErrors, SerenityWarnings)
    }
}
