package net.derfruhling.serenity.compiler.ir

import net.derfruhling.serenity.compiler.NotApplicable
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class SerenityIrGenerationExtension : IrGenerationExtension {
    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext
    ) {
        val transformers = listOf(
            ::IrConstantNameGenerator,
            ::IrSidedFunctionBodyDeleter
        )

        for (transformer in transformers) {
            try {
                transformer(pluginContext).lower(moduleFragment)
            } catch (_: NotApplicable) {
            }
        }
    }
}