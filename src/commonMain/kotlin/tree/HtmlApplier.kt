package net.derfruhling.serenity.tree

import androidx.compose.runtime.Applier
import net.derfruhling.serenity.tree.platform.ComposeNode

interface HtmlApplier : Applier<ComposeNode> {
    var reflowTransformer: ((String) -> String)?
}
