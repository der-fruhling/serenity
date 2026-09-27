package net.derfruhling.serenity.elements

import net.derfruhling.serenity.PageHolder

actual fun PageHolder<*>.constructActualAddress(linkBase: String): String = buildString {
    if (linkBase.isNotEmpty()) {
        if (!linkBase.startsWith('/')) append('/')
        append(urlEncode(linkBase))
    }

    if (!path.startsWith('/')) append('/')
    append(urlEncode(path))

    if (hash.isNotEmpty()) {
        for ((i, entry) in hash.entries.withIndex()) {
            val (key, param) = entry
            append(if (i == 0) '#' else '&')

            append(urlEncode(key))
            if (param.isNotEmpty()) {
                append('=')
                append(param)
            }
        }
    }
}
