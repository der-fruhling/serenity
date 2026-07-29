package net.derfruhling.serenity.testapp

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSerializable
import io.github.oshai.kotlinlogging.KotlinLogging
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.annotations.Client
import net.derfruhling.serenity.elements.Button
import net.derfruhling.serenity.elements.Content
import net.derfruhling.serenity.elements.FlexColumn
import net.derfruhling.serenity.elements.Footer
import net.derfruhling.serenity.elements.Header
import net.derfruhling.serenity.elements.Link
import net.derfruhling.serenity.elements.Page
import net.derfruhling.serenity.elements.PageLayout
import net.derfruhling.serenity.elements.UnorderedList

private val logger = KotlinLogging.logger {}

@Composable
@Page("/")
fun IndexPage() = PageLayout("Hello, world!") {
    Header {
        Text("Header")
    }

    Content {
        FlexColumn {
            Link("Buttons", ButtonsPage)
            Link("Save data", SaveDataPage)
        }
    }

    Footer {
        Text("Footer")
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
