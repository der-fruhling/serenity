package net.derfruhling.serenity.compiler

import net.derfruhling.serenity.compiler.fir.TypeSideAttributeApplier
import net.derfruhling.serenity.compiler.fir.SidedAnnotationCheckerExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class SerenityPluginRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::TypeSideAttributeApplier
        +::SidedAnnotationCheckerExtension
    }
}
