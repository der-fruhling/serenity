package net.derfruhling.html.testapp

import net.derfruhling.html.PageRegistry
import net.derfruhling.html.register

fun PageRegistry.registerPages() {
    register(IndexPage)
    register(ButtonsPage)
    register(SaveDataPage)
}
