package net.derfruhling.html.test

import net.derfruhling.html.Name
import net.derfruhling.html.tree.composeHtmlOnce
import net.derfruhling.html.elements.Bold
import net.derfruhling.html.elements.Page
import net.derfruhling.html.elements.Paragraph
import net.derfruhling.html.Formatter.Companion.formatStringDebug
import net.derfruhling.html.tree.textContext
import net.derfruhling.html.tree.tree
import kotlin.test.Test
import kotlin.test.assertEquals

class ContentTest {
    @Test
    fun `big paragraph`() = useContext {
        val tree = composeHtmlOnce {
            Page("paragraph") {
                Paragraph("""
                    Paragraph!!!
                """.trimIndent())

                Paragraph("""
                    Paragraph 2!!!
                """.trimIndent())
            }
        }

        println(formatStringDebug(tree::format))

        val expected = tree {
            page("paragraph") {
                element("p") {
                    text("Paragraph!!!")
                }

                element("p") {
                    text("Paragraph 2!!!")
                }
            }
        }

        println(formatStringDebug(expected::format))

        assertEquals(expected, tree)
    }

    @Test
    fun `bold text`() = useContext {
        val tree = composeHtmlOnce {
            Page("bold text") {
                Bold("""
                    Miaw
                """.trimIndent())
            }
        }

        val b = tree.rootElement.findDescendentNamed(Name.of("b"))!!
        assertEquals("Miaw", b.textContext)
    }
}