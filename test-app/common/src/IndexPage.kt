package net.derfruhling.serenity.testapp

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSerializable
import io.github.oshai.kotlinlogging.KotlinLogging
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.annotations.ClientOnly
import net.derfruhling.serenity.annotations.RegisterPage
import net.derfruhling.serenity.elements.Link
import net.derfruhling.serenity.elements.form.Button
import net.derfruhling.serenity.elements.form.Select
import net.derfruhling.serenity.elements.form.TextInput
import net.derfruhling.serenity.elements.form.TextInputType
import net.derfruhling.serenity.elements.layout.Content
import net.derfruhling.serenity.elements.layout.FlexColumn
import net.derfruhling.serenity.elements.layout.Footer
import net.derfruhling.serenity.elements.layout.Header

private val logger = KotlinLogging.logger {}

@Composable
@RegisterPage("/", title = "Hello, world!")
fun IndexPage() {
    Header {
        Text("Header")
    }

    Content {
        FlexColumn {
            Link("Buttons", ButtonsPage.of(start = 0))
            Link("Save data", SaveDataPage)
        }
    }

    Footer {
        Text("Footer")
    }
}

@Composable
@RegisterPage("/buttons/{start}", title = "Buttons")
fun ButtonsPage(start: Int) {
    var count by remember { mutableIntStateOf(start) }

    Button(
        "Click count: $count",
        onClick = @ClientOnly {
            count++
            logger.debug { "Clicked! $count" }
        }
    )
}

@Composable
@RegisterPage("/save-data", title = "Buttons")
fun SaveDataPage() {
    var count by rememberSerializable { mutableIntStateOf(0) }

    Button(
        "Click count: $count",
        onClick = @ClientOnly {
            count++
            logger.debug { "Clicked! $count" }
        }
    )
}

@Composable
@RegisterPage("/inputs")
fun InputsPage() {
    FlexColumn {
        var plainText by remember { mutableStateOf("<hello!>") }
        TextInput(placeholder = "Plain", onChange = { plainText = it })

        Text(plainText)

        var searchText by remember { mutableStateOf("<search!>") }
        TextInput(
            type = TextInputType.SEARCH,
            placeholder = "Search the site...",
            onChange = { searchText = it }
        )

        Text(searchText)

        var selection by remember { mutableStateOf("nothing") }
        Select(onChange = { selection = it }) {
            Option("nothing", text = "Make a selection", selected = true, disabled = true)
            Option("miaw", text = "Miaw")
            Option("cat", text = "Cat")
            Option("silly", text = "Silly")
        }

        Text("Current section: $selection")
    }
}
