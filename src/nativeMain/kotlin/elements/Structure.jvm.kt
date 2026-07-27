package net.derfruhling.html.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.derfruhling.html.tree.StructureProvider
import net.derfruhling.html.annotations.HtmlComposable

var structureProvider: StructureProvider? by mutableStateOf(null as StructureProvider?)

@Composable
@HtmlComposable
actual fun HeadContext.IncludeScript(async: Boolean, defer: Boolean) {
    structureProvider?.includeScripts?.forEach { source ->
        script(source, async, defer)
    }
}
