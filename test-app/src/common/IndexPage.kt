package net.derfruhling.html.testapp

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSerializable
import io.github.oshai.kotlinlogging.KotlinLogging
import net.derfruhling.html.annotations.Client
import net.derfruhling.html.elements.Button
import net.derfruhling.html.elements.Link
import net.derfruhling.html.elements.Page
import net.derfruhling.html.elements.UnorderedList

private val logger = KotlinLogging.logger {}

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

    Button("Click count: $count", onClick = @Client {
        count++
        logger.debug { "Clicked! $count" }
    })
}

@Composable
@Page("/save-data")
fun SaveDataPage() = Page("Buttons") {
    var count by rememberSerializable { mutableIntStateOf(0) }

    Button("Click count: $count", onClick = @Client {
        count++
        logger.debug { "Clicked! $count" }
    })
}
