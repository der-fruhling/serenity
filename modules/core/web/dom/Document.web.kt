@file:Suppress("RedundantNullableReturnType")

package net.derfruhling.serenity.dom

import net.derfruhling.serenity.tree.platform.CURRENT
import net.derfruhling.serenity.tree.platform.RealDocument
import web.dom.Document
import web.dom.document
import net.derfruhling.serenity.tree.platform.Document as PlatformDocument

actual typealias Document = Document

@OptIn(ExperimentalWasmJsInterop::class)
private fun getDocumentNode(document: Document): JsReference<PlatformDocument>? =
    js("document.__serenity_node")

@OptIn(ExperimentalWasmJsInterop::class)
private fun setDocumentNode(document: Document, platform: JsReference<PlatformDocument>): Unit =
    js("document.__serenity_node = platform")

@OptIn(ExperimentalWasmJsInterop::class)
private fun isUndefined(any: JsAny): Boolean = js("any === undefined")

@OptIn(ExperimentalWasmJsInterop::class)
actual val Document.node: PlatformDocument
    get() = getDocumentNode(this)?.get() ?: when(this) {
        document -> PlatformDocument.CURRENT
        else -> PlatformDocument(RealDocument(this))
    }.also { setDocumentNode(this, it.toJsReference()) }
