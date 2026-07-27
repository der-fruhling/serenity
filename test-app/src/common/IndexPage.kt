package net.derfruhling.html.testapp

import androidx.compose.runtime.*
import co.touchlab.kermit.Logger
import net.derfruhling.html.elements.Button
import net.derfruhling.html.elements.Link
import net.derfruhling.html.elements.Page
import net.derfruhling.html.elements.UnorderedList

@Composable
@Page("/")
fun IndexPage() = Page("Hello, world!") {
    UnorderedList {
        Entry { Link("Buttons", ButtonsPage) }
    }
}

@Composable
@Page("/buttons")
fun ButtonsPage() = Page("Buttons") {
    var count by remember { mutableIntStateOf(0) }

    Button("Click count: $count", onClick = {
        count++
        Logger.d { "Clicked! $count" }
    })
}
