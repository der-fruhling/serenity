package net.derfruhling.serenity.tree

import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.ReusableComposition
import net.derfruhling.serenity.platform.CURRENT
import net.derfruhling.serenity.platform.Document
import net.derfruhling.serenity.platform.PlatformApplier

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
