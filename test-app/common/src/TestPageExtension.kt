package net.derfruhling.serenity.testapp

import net.derfruhling.serenity.Text
import net.derfruhling.serenity.modularity.extension.AbstractPageExtensionVoid
import net.derfruhling.serenity.modularity.extension.PageExtensionPoints

object TestPageExtension : AbstractPageExtensionVoid() {
    override fun initialize(extends: PageExtensionPoints) {
        extends.body before {
            Text("Injected content")
        }
    }
}
