package net.derfruhling.serenity.dom

import net.derfruhling.serenity.event.EventTarget
import net.derfruhling.serenity.tree.platform.Document
import net.derfruhling.serenity.dom.Document as DomDocument

expect class Document : EventTarget {
}

expect val DomDocument.node: Document
