package net.derfruhling.serenity.platform

import androidx.compose.runtime.Applier

interface HtmlApplier : Applier<ComposeNode> {
    var reflowTransformer: ((String) -> String)?
}
