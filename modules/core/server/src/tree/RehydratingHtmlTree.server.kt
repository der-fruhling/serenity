package net.derfruhling.serenity.tree

import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.ReusableComposition
import net.derfruhling.serenity.tree.platform.Document

fun RehydratingHtmlTree(
    parent: CompositionContext,
    createDocument: () -> Document,
    applier: (Document) -> HtmlApplier
): RehydratingHtmlTree<Document> {
    val doc = createDocument()
    val applier = applier(doc)
    return RehydratingHtmlTree(
        root = doc,
        applier = applier,
        composition = ReusableComposition(applier, parent)
    )
}

fun RehydratingHtmlTree(
    parent: CompositionContext,
    baseUri: String,
    applier: (Document) -> HtmlApplier
): RehydratingHtmlTree<Document> {
    return RehydratingHtmlTree(
        parent,
        createDocument = { Document(baseUri) },
        applier
    )
}
