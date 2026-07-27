package net.derfruhling.html.testapp

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import net.derfruhling.html.Name
import net.derfruhling.html.attribute.Attributes
import net.derfruhling.html.ktor.server.ComposeHtml
import net.derfruhling.html.ktor.server.pageFunctionName
import net.derfruhling.html.ktor.server.register
import net.derfruhling.html.ktor.server.serveStatic
import net.derfruhling.html.tree.platform.ElementNode
import net.derfruhling.html.tree.platform.TextNode
import net.derfruhling.html.tree.platform.head

fun Application.configure() {
    install(ComposeHtml) {
        registerTransformation {
            val hash = attributes[pageFunctionName]

            val head = it.head!!
            head.add(ElementNode().apply element@ {
                name = Name.of("script")

                attribute(Attributes.src, "/_/js/page.js")
            })

            head.add(ElementNode().apply element@ {
                name = Name.of("script")

                add(TextNode().apply {
                    //language=javascript
                    textContent = """
                        const entryFn = window["test-app"]["$hash"];
                        if(entryFn) entryFn();
                        else console.warn("Entry function for", "$hash", "not found");
                    """.trimIndent()
                })
            })

            it
        }
    }

    routing {
        serveStatic()

        register(IndexPage)
        register(ButtonsPage)
    }
}
