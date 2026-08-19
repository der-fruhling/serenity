package net.derfruhling.serenity.tests

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import js.array.asList
import kotlinx.coroutines.test.runTest
import net.derfruhling.serenity.elements.form.Button
import net.derfruhling.serenity.test.runDomComposeTest
import web.html.HTMLButtonElement
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ButtonTest {
    @Test
    fun `click event`() = runTest {
        var isPressed by mutableStateOf(false)
        runDomComposeTest({
            Button("Hello, world!", onClick = {
                isPressed = true
            })
        }) {
            useSnapshot { assertFalse(isPressed) }

            val button = it.body.getElementsByTagName("button").asList().single()
            (button as HTMLButtonElement).click()
            awaitIdle()

            useSnapshot { assertTrue(isPressed) }
        }
    }
}
