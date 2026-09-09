package net.derfruhling.serenity.platform

import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.ReusableComposition

fun RehydratingHtmlTree(
    parent: CompositionContext,
    document: Document,
    applier: (Document) -> HtmlApplier
): RehydratingHtmlTree<Document> {
    val applier = applier(document)
    return RehydratingHtmlTree(
        root = document,
        applier = applier,
        composition = ReusableComposition(applier, parent)
    )
}

fun RehydratingHtmlTree(
    parent: CompositionContext,
    applier: (Document) -> HtmlApplier
): RehydratingHtmlTree<Document> {
    return RehydratingHtmlTree(
        parent,
        Document.CURRENT,
        applier
    )
}

fun RehydratingHtmlTree(
    parent: CompositionContext
): RehydratingHtmlTree<Document> {
    return RehydratingHtmlTree(parent, ::PlatformApplier)
}
