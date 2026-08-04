package net.derfruhling.serenity

actual fun navigate(to: PageHolder<*>) {
    WebEntryPoint.current.setPage(to)
}

actual fun navigateDirect(to: PageHolder<*>) {
    WebEntryPoint.current.setPageDirect(to)
}
