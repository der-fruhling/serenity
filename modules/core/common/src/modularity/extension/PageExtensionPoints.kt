package net.derfruhling.serenity.modularity.extension

import androidx.compose.runtime.Composable

interface PageExtensionPoints {
    val head: PageExtensionPoint<@Composable () -> Unit>
    val body: PageExtensionPoint<@Composable () -> Unit>
}
