package net.derfruhling.serenity.modularity

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object Modules {
    private val loadedModules = mutableSetOf<Module>()
    val onChanged: Event<() -> Unit> field = Event()

    fun install(fn: ModuleInstaller.() -> Unit) {
        InstallerImpl().apply(fn).done()
        onChanged()
    }

    suspend fun createProvidedValues(context: ProvideContext) = coroutineScope {
        loadedModules.forEach {
            launch { it.useProvide(context) }
        }
    }

    private class InstallerImpl : ModuleInstaller {
        private val toUse = mutableListOf<Module>()

        override fun use(module: Module) {
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
