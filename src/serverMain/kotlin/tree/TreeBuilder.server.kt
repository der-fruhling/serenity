package net.derfruhling.serenity.tree

import net.derfruhling.serenity.tree.platform.Document

actual fun getDocumentForTesting(): Document {
    return Document("https://example.com")
}