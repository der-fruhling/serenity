package net.derfruhling.html.testapp

import androidx.compose.runtime.*
import co.touchlab.kermit.Logger
import net.derfruhling.html.elements.Button
import net.derfruhling.html.elements.Page

@Composable
@Page("/")
fun IndexPage() = Page("Hello, world!") {
    var count by remember { mutableIntStateOf(0) }

    Button("Click count: $count", onClick = {
        count++
        Logger.d { "Clicked! $count" }
    })
}
