package net.derfruhling.serenity.tree

import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.snapshots.MutableSnapshot
import androidx.compose.runtime.snapshots.Snapshot
import net.derfruhling.serenity.SerialSavedData
import net.derfruhling.serenity.tree.platform.Document

fun RehydratingHtmlTree(
    parent: CompositionContext,
    createDocument: () -> Document,
    applier: (Document) -> HtmlApplier,
    restoreValues: SerialSavedData? = null
): RehydratingHtmlTree<Document> {
    val doc = createDocument()
    val applier = applier(doc)
    return RehydratingHtmlTree(
        root = doc,
        applier = applier,
        composition = Composition(applier, parent),
        saveableRegistry = SaveableStateRegistry(restoreValues?.items()) { true }
    )
}

fun RehydratingHtmlTree(
    parent: CompositionContext,
    baseUri: String,
    applier: (Document) -> HtmlApplier,
    restoreValues: SerialSavedData? = null
): RehydratingHtmlTree<Document> {
    return RehydratingHtmlTree(
        parent,
        createDocument = { Document(baseUri) },
        applier,
        restoreValues
    )
}
