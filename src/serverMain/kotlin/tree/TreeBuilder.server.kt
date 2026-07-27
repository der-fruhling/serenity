package net.derfruhling.html.tree

import net.derfruhling.html.tree.platform.Document

actual fun getDocumentForTesting(): Document {
    return Document("https://example.com")
}