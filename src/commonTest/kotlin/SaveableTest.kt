package net.derfruhling.html.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import net.derfruhling.html.SerialRegistry
import net.derfruhling.html.SerialSavedData
import net.derfruhling.html.tree.composeHtml
import net.derfruhling.html.elements.Page
import net.derfruhling.html.elements.unaryMinus
import net.derfruhling.html.tree.findDescendentNamed
import net.derfruhling.html.tree.textContext
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveableTest {
    @Serializable
    data class HelloWorld(val test: Int)

    init {
        SerialRegistry.register<HelloWorld>()
    }

    @Composable
    private fun saveableTest() {
        Page("save test") {
            var item by rememberSerializable { mutableStateOf(HelloWorld(test = 42)) }

            -"${item.test}"

            item = item.copy(test = item.test + 5)
        }
    }

    @Test
    fun `test saveable`() = useContext {
        val html = composeHtml(this::saveableTest)
        html.resume()

        assertEquals("42", html.rootElement.findDescendentNamed("p")!!.textContext)

        val savedData = html.save()
        println(savedData)
        val json = SerialRegistry.encode(savedData)
        println(json)

        val decoded = SerialRegistry.decode<SerialSavedData>(json)
        println(decoded)

        val newHtml = composeHtml(decoded, this::saveableTest)
        newHtml.resume()

        assertEquals("47", newHtml.rootElement.findDescendentNamed("p")!!.textContext)
    }
}