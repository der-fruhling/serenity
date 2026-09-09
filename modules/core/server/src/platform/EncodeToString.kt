package net.derfruhling.serenity.platform

import com.fleeksoft.ksoup.nodes.DocumentType

fun Document.encodeToString(): String {
    if (real.node.documentType() == null) {
        real.node.prependChild(DocumentType("html", "", ""))
    }
    return real.node.html()
}
