package net.derfruhling.serenity.testapp

import net.derfruhling.serenity.PageRegistry
import net.derfruhling.serenity.register

fun PageRegistry.registerPages() {
    register(IndexPage)
    register(ButtonsPage)
    register(SaveDataPage)
}
