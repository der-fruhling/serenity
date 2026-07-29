package net.derfruhling.serenity.testapp

import net.derfruhling.serenity.PageRegistry
import net.derfruhling.serenity.register

fun PageRegistry.registerPages() {
    head {
        useScript("/_/js/page.js")
        useStylesheet("/_/style.css")
        useStylesheet("/_/net.derfruhling.serenity/serenity/builtin/serenity.css")
        useEntrypoint("test-app")
    }

    register(IndexPage)
    register(ButtonsPage)
    register(SaveDataPage)
}
