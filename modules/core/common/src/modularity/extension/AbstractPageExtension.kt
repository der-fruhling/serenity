package net.derfruhling.serenity.modularity.extension

import net.derfruhling.serenity.PageContract

abstract class AbstractPageExtension<Cfg> {
    abstract fun configure(page: PageContract, fn: Cfg.() -> Unit)
    abstract fun initialize(extends: PageExtensionPoints)
}

abstract class AbstractPageExtensionVoid : AbstractPageExtension<Nothing>() {
    final override fun configure(page: PageContract, fn: Nothing.() -> Unit) {}
}
