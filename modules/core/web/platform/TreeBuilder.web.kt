package net.derfruhling.serenity.tree

import net.derfruhling.serenity.platform.CURRENT
import net.derfruhling.serenity.platform.Document

actual fun getDocumentForTesting(): Document {
    return Document.CURRENT
}
