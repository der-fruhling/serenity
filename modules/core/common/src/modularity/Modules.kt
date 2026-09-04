package net.derfruhling.serenity.modularity

object Modules {
    private val loadedModules = mutableSetOf<Module>()

    fun install(fn: ModuleInstaller.() -> Unit) {
        InstallerImpl().apply(fn).done()
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
