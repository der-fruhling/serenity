package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.tree.StructureProvider
import net.derfruhling.serenity.annotations.HtmlComposable
import java.util.ServiceLoader

val provider by lazy { ServiceLoader.load(StructureProvider::class.java).firstOrNull() }

@Composable
@HtmlComposable
actual fun HeadContext.IncludeScript(async: Boolean, defer: Boolean) {
    provider?.includeScripts?.forEach { source ->
        script(source, async, defer)
    }
}
