package net.derfruhling.serenity.elements

import net.derfruhling.serenity.PageHolder
import web.url.URLSearchParams

@OptIn(ExperimentalWasmJsInterop::class)
actual fun PageHolder<*>.constructActualAddress(linkBase: String): String = buildString {
    if(linkBase.isNotEmpty()) {
        if(!linkBase.startsWith('/')) append('/')
        append(urlEncodePath(linkBase))
    }

    if(!path.startsWith('/')) append('/')
    append(urlEncodePath(path))

    if(hash.isNotEmpty()) {
        val urlSearchParams = URLSearchParams()

        for((key, value) in hash) {
            urlSearchParams.append(key, value)
        }

        append("#$urlSearchParams")
    }
}
