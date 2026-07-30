package net.derfruhling.serenity.tree

import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.ReusableComposition
import androidx.compose.runtime.saveable.SaveableStateRegistry
import net.derfruhling.serenity.Name
import net.derfruhling.serenity.SerialRegistry
import net.derfruhling.serenity.SerialSavedData
import net.derfruhling.serenity.attribute.Attributes
import net.derfruhling.serenity.tree.platform.CURRENT
import net.derfruhling.serenity.tree.platform.Document
import net.derfruhling.serenity.tree.platform.PlatformApplier
import net.derfruhling.serenity.tree.platform.textContent

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
