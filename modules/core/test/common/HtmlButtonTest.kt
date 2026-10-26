package net.derfruhling.serenity.tests

import kotlinx.coroutines.test.runTest
import net.derfruhling.serenity.Formatter
import net.derfruhling.serenity.elements.form.Button
import net.derfruhling.serenity.event.ClickEvent
import net.derfruhling.serenity.platform.tree
import net.derfruhling.serenity.test.assertEquivalence
import net.derfruhling.serenity.test.runComposeTest
import kotlin.test.Test

class HtmlButtonTest {
    @Test
    fun testButtonTree() = runTest {
        runComposeTest(fn = {
            Button(label = "Label", onClick = {})
        }) {
            assertEquivalence(tree {
                element("button") {
                    text("Label")
                    eventHandler(ClickEvent)
                }
            }, it)
        }
    }
}