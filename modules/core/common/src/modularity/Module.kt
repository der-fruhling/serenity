package net.derfruhling.serenity.modularity

abstract class Module<Config : Any>(
    val name: String
) {
    private var _isInitialized = false
    internal open lateinit var mutableConfig: Config

    val isInitialized: Boolean by ::_isInitialized
    val config: Config by ::mutableConfig

    abstract fun defaultConfig(): Config

    protected abstract fun initialize()
    protected open fun moduleAdded(module: Module<*>) {}

    protected open suspend fun ProvideContext.provide() {}

    internal fun initialize(addedModules: Set<Module<*>>) {
        if(!_isInitialized) {
            initialize()
            _isInitialized = true
        }

        for(added in addedModules) {
            moduleAdded(added)
        }
    }

    open suspend fun CommonContext.asyncInit() {}

    suspend fun useProvide(context: ProvideContext) {
        with(context) {
            provide()
        }
    }
}

abstract class SimpleModule(name: String) : Module<Unit>(name) {
    final override var mutableConfig: Unit
        get() = Unit
        set(_) {}

    final override fun defaultConfig() = Unit
}
