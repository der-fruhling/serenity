package net.derfruhling.serenity.compiler

import net.derfruhling.serenity.compiler.ir.SerenityIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

class SerenityPluginComponentRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String
        get() = "net.derfruhling.serenity"
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(
        configuration: CompilerConfiguration
    ) {
        FirExtensionRegistrarAdapter.registerExtension(SerenityPluginRegistrar())
        IrGenerationExtension.registerExtension(SerenityIrGenerationExtension())
    }
}