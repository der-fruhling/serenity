package net.derfruhling.serenity.platform

import com.fleeksoft.ksoup.nodes.Document.OutputSettings
import com.fleeksoft.ksoup.nodes.DocumentType

fun Document.encodeToString(): String {
    if (real.node.documentType() == null) {
        real.node.prependChild(DocumentType("html", "", ""))
    }
    real.node.outputSettings(OutputSettings(
        prettyPrint = false
    ))
    return real.node.html()
}
