package net.derfruhling.serenity.modularity.extension

import net.derfruhling.serenity.PageContract

abstract class AbstractPageExtensionVoid : AbstractPageExtension<Nothing>() {
    final override fun configure(page: PageContract, fn: Nothing.() -> Unit) {}
}
