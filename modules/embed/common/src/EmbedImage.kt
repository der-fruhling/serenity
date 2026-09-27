package net.derfruhling.serenity.embeds

import androidx.compose.runtime.Immutable

@EmbedsDsl
class MutableEmbedImage(val url: String) : Extensible() {
    var width: Int = -1
    var height: Int = -1
    var type: String? = null
    var alt: String? = null
}

@Immutable
class EmbedImage(from: MutableEmbedImage) : Extended(from) {
    val url: String = from.url
    val width: Int = from.width
    val height: Int = from.height
    val type: String? = from.type
    val alt: String? = from.alt
}

inline fun <T> Int.maybe(fn: (Int) -> T): T? {
    return if(this != -1) {
        fn(this)
    } else null
}
