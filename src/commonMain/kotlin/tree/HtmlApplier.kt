package net.derfruhling.html.tree

import androidx.compose.runtime.Applier
import net.derfruhling.html.tree.platform.ComposeNode

interface HtmlApplier : Applier<ComposeNode> {
    var reflowTransformer: ((String) -> String)?
}
