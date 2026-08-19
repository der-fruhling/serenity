package net.derfruhling.serenity

expect val currentPage: PageHolder<*>

expect fun navigate(to: PageHolder<*>)
expect fun navigateDirect(to: PageHolder<*>)
