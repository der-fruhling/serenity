package net.derfruhling.serenity.tree

import net.derfruhling.serenity.tree.platform.CURRENT
import net.derfruhling.serenity.tree.platform.Document

actual fun getDocumentForTesting(): Document {
    return Document.CURRENT
}
