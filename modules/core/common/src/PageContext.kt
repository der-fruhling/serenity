package net.derfruhling.serenity

import net.derfruhling.serenity.annotations.ClientOnly
import net.derfruhling.serenity.annotations.ServerOnly

abstract class PageContext {
    abstract fun getParameter(name: String): String?
    abstract fun registerInitFun(fn: suspend PageInit.() -> Unit)

    @ServerOnly
    fun registerServerInitFun(fn: suspend PageServerInit.() -> Unit) =
        registerInitFun { if(server) fn() }

    @ClientOnly
    fun registerClientInitFun(fn: suspend PageClientInit.() -> Unit) =
        registerInitFun { if(client) fn() }
}
