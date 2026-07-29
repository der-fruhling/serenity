package net.derfruhling.serenity.testapp

import net.derfruhling.serenity.PageRegistry

fun PageRegistry.registerPages() {
    head {
        useScript("/_/js/page.js")
        useStylesheet("/_/style.css")
        useStylesheet("/_/net.derfruhling.serenity/serenity-core/builtin/serenity.css")
        useEntrypoint("test-app")
    }

    register(IndexPage)
    register(ButtonsPage)
    register(SaveDataPage)
}
