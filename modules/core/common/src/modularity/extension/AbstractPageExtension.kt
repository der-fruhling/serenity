package net.derfruhling.serenity.modularity.extension

abstract class AbstractPageExtension<Cfg> {
    abstract fun configure(fn: Cfg.() -> Unit)
    abstract fun initialize(extends: PageExtensionPoints)
}

abstract class AbstractPageExtensionVoid : AbstractPageExtension<Nothing>() {
    final override fun configure(fn: Nothing.() -> Unit) {}
}
