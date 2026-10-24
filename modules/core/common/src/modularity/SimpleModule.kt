package net.derfruhling.serenity.modularity

abstract class SimpleModule(name: String) : Module<Unit>(name) {
    final override var mutableConfig: Unit
        get() = Unit
        set(_) {}

    final override fun defaultConfig() = Unit
}
