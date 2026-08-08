package net.derfruhling.serenity.test

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import net.derfruhling.serenity.Name
import net.derfruhling.serenity.elements.Button
import net.derfruhling.serenity.tree.platform.ElementNode

class TheTestTest : CompositionSpec({
    "Miaw" suspends {
        val tree = runStaticComposeTest {
            Button("Miaw")
        }

        val child = tree.children.first().shouldBeInstanceOf<ElementNode>()
        child.name shouldBe Name.of("button")
    }
}) {
}