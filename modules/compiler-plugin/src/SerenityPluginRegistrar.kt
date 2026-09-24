package net.derfruhling.serenity.compiler

import net.derfruhling.serenity.compiler.fir.FirPageDeclarationGenerator
import net.derfruhling.serenity.compiler.fir.SidedAnnotationCheckerExtension
import net.derfruhling.serenity.compiler.fir.StabilityCheckerExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class SerenityPluginRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::FirPageDeclarationGenerator
        +::SidedAnnotationCheckerExtension
        +::StabilityCheckerExtension
    }
}
