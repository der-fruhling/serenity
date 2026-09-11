package net.derfruhling.serenity.modularity

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

object Modules {
    private val loadedModules = mutableSetOf<Module<*>>()
    val onChanged: Event<() -> Unit> field = Event()

    fun install(fn: ModuleInstaller.() -> Unit) {
        InstallerImpl().apply(fn).done()
        onChanged()
    }

    suspend fun asyncInit(context: CommonContext) = coroutineScope {
        loadedModules.forEach {
            launch { with(it) { context.asyncInit() } }
        }
    }

    suspend fun createProvidedValues(context: ProvideContext) = coroutineScope {
        loadedModules.forEach {
            launch { it.useProvide(context) }
        }
    }

    operator fun <T : Any> get(kClass: KClass<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return loadedModules.firstOrNull { kClass.isInstance(it) } as T?
    }

    inline fun <reified T : Any> the(): T? {
        return get(T::class)
    }

    private class InstallerImpl : ModuleInstaller {
        private val toUse = mutableListOf<Module<*>>()

        override fun <Config : Any> use(module: Module<Config>, fn: Config.() -> Unit) {
            val config = module.defaultConfig().apply(fn)
            module.mutableConfig = config
            toUse.add(module)
        }

        override fun use(module: Module<Unit>) {
            toUse.add(module)
        }

        fun done() {
            val toInit = toUse.filter { it !in loadedModules }
            val initSet = toInit.toSet()
            val fullSet = loadedModules + initSet
            val existingModules = loadedModules.toSet()

            for(module in toInit) {
                module.initialize(fullSet)
                loadedModules.add(module)
            }

            for(existing in existingModules) {
                existing.initialize(initSet)
            }
        }
    }
}
