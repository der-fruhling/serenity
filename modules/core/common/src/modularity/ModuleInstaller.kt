package net.derfruhling.serenity.modularity

sealed interface ModuleInstaller {
    fun <Config : Any> use(module: Module<Config>, fn: Config.() -> Unit = {})
    fun use(module: Module<Unit>)
}
