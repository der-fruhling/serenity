package net.derfruhling.html.test

import net.derfruhling.html.tree.composeHtmlOnce
import net.derfruhling.html.tree.classes
import net.derfruhling.html.elements.Div
import net.derfruhling.html.elements.Page
import net.derfruhling.html.elements.Span
import net.derfruhling.html.elements.attributes
import net.derfruhling.html.tree.findDescendentNamed
import kotlin.test.Test
import kotlin.test.assertContains

class ComposeTest {
    @Test
    fun `test classes`() = useContext {
        val tree = composeHtmlOnce {
            Page("hello world!") {
                Div {
                    attributes {
                        classes("Test")
                    }
                }
            }
        }

        val div = tree.rootElement.findDescendentNamed("div")!!
        assertContains(div.classes, "Test")
    }

    @Test
    fun `test classes concise`() = useContext {
        val tree = composeHtmlOnce {
            Page("hello world!") {
                Div("Test")
            }
        }

        val div = tree.rootElement.findDescendentNamed("div")!!
        assertContains(div.classes, "Test")
    }

    @Test
    fun `test span classes`() = useContext {
        val tree = composeHtmlOnce {
            Page("hello world!") {
                Span {
                    attributes {
                        classes("Test")
                    }
                }
            }
        }

        val div = tree.rootElement.findDescendentNamed("span")!!
        assertContains(div.classes, "Test")
    }

    @Test
    fun `test span classes concise`() = useContext {
        val tree = composeHtmlOnce {
            Page("hello world!") {
                Span("Test")
            }
        }

        val div = tree.rootElement.findDescendentNamed("span")!!
        assertContains(div.classes, "Test")
    }
}