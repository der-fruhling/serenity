package net.derfruhling.serenity.modularity

abstract class Module(
    val name: String
) {
    private var isInitialized = false

    protected abstract fun initialize()
    protected open fun moduleAdded(module: Module) {}

    protected open suspend fun ProvideContext.provide() {}

    fun initialize(addedModules: Set<Module>) {
        if(!isInitialized) {
            initialize()
            isInitialized = true
        }

        for(added in addedModules) {
            moduleAdded(added)
        }
    }

    suspend fun useProvide(context: ProvideContext) {
        with(context) {
            provide()
        }
    }
}
