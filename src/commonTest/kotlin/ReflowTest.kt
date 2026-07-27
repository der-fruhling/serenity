package net.derfruhling.html.test

import net.derfruhling.html.tree.Text
import net.derfruhling.html.tree.composeHtmlOnce
import net.derfruhling.html.elements.reflow
import net.derfruhling.html.unaryPlus
import kotlin.test.Test
import kotlin.test.assertEquals

class ReflowTest {
    @Test
    fun reflowTrimsIndent() = useContext {
        val tree = composeHtmlOnce {
            +"""
                Hello, world!
            """.reflow
        }

        assertEquals("Hello, world!", (tree.rootNode as Text).textContent)
    }
}