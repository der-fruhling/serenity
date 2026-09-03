package net.derfruhling.serenity.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

class SerenityCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String
        get() = "net.derfruhling.serenity"
    override val pluginOptions: Collection<AbstractCliOption>
        get() = emptyList()

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        error("Unexpected config option: '${option.optionName}'")
    }
}