package net.derfruhling.serenity.modularity

sealed interface ModuleInstaller {
    fun use(module: Module)
}
