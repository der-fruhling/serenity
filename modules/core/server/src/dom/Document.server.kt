package net.derfruhling.serenity.dom

import net.derfruhling.serenity.event.EventTarget
import net.derfruhling.serenity.tree.platform.Document
import net.derfruhling.serenity.dom.Document as DomDocument

actual class Document(val real: Document) : EventTarget()

actual val DomDocument.node: Document
    get() = real