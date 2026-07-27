package net.derfruhling.html.tree

import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.snapshots.Snapshot
import net.derfruhling.html.Name
import net.derfruhling.html.SerialRegistry
import net.derfruhling.html.SerialSavedData
import net.derfruhling.html.attribute.Attributes
import net.derfruhling.html.tree.platform.CURRENT
import net.derfruhling.html.tree.platform.Document
import net.derfruhling.html.tree.platform.PlatformApplier
import net.derfruhling.html.tree.platform.textContent

fun RehydratingHtmlTree(
    parent: CompositionContext,
    document: Document,
    applier: (Document) -> HtmlApplier,
    restoreValues: SerialSavedData?
): RehydratingHtmlTree<Document> {
    val applier = applier(document)
    return RehydratingHtmlTree(
        root = document,
        applier = applier,
        composition = Composition(applier, parent),
        saveableRegistry = SaveableStateRegistry(restoreValues?.items()) { true }
    )
}

fun RehydratingHtmlTree(
    parent: CompositionContext,
    applier: (Document) -> HtmlApplier,
    restoreValues: SerialSavedData?
): RehydratingHtmlTree<Document> {
    return RehydratingHtmlTree(
        parent,
        Document.CURRENT,
        applier,
        restoreValues
    )
}

fun RehydratingHtmlTree(
    parent: CompositionContext,
    applier: (Document) -> HtmlApplier
): RehydratingHtmlTree<Document> {
    val doc = Document.CURRENT
    val restoreValues = doc.findImmediateElementNamed(Name.of("head"))
        ?.findImmediateElementNamed(Name.of("script")) {
            it.attribute(Attributes.type) == "application/json+x-compose-shared"
        }
        ?.textContent
        ?.let { SerialRegistry.decode<SerialSavedData>(it) }

    return RehydratingHtmlTree(
        parent,
        doc,
        applier,
        restoreValues
    )
}

fun RehydratingHtmlTree(
    parent: CompositionContext
): RehydratingHtmlTree<Document> {
    return RehydratingHtmlTree(parent, ::PlatformApplier)
}
