package net.derfruhling.serenity.testapp

import net.derfruhling.serenity.Text
import net.derfruhling.serenity.modularity.extension.AbstractPageExtension
import net.derfruhling.serenity.modularity.extension.PageExtensionPoints

class TestPageExtension : AbstractPageExtension() {
    override fun initialize(extends: PageExtensionPoints) {
        extends.body before {
            Text("Injected content")
        }
    }
}
