package net.derfruhling.serenity.modularity.extension

import androidx.compose.runtime.Composable

internal class PageExtensionInstance : PageExtensionPoints {
    override val head: PageExtensionPoint<@Composable (() -> Unit)> = PageExtensionPoint(Head)
    override val body: PageExtensionPoint<@Composable (() -> Unit)> = PageExtensionPoint(Body)

    fun reset() {
        head.reset()
        body.reset()
    }
}
