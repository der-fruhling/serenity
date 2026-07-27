package net.derfruhling.html.tree

import com.fleeksoft.ksoup.nodes.DocumentType
import net.derfruhling.html.tree.platform.Document

fun Document.encodeToString(): String {
    if(real.node.documentType() == null) {
        real.node.prependChild(DocumentType("html", "", ""))
    }
    return real.node.html()
}
