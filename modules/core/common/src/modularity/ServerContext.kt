package net.derfruhling.serenity.modularity

import net.derfruhling.serenity.annotations.ServerOnly

@ServerOnly
interface ServerContext {
    fun getHeader(name: String): String?
}
