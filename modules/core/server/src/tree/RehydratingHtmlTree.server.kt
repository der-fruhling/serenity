package net.derfruhling.serenity.tree

import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.ReusableComposition
import androidx.compose.runtime.saveable.SaveableStateRegistry
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
        composition = ReusableComposition(applier, parent),
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
