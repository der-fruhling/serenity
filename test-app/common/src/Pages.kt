package net.derfruhling.serenity.testapp

import net.derfruhling.serenity.PageRegistry
import net.derfruhling.serenity.elements.Page

fun PageRegistry<PlatformContext>.registerPages() {
    template {
        Page {
            Head {
                SlotHead()

                useStylesheet("/_/style.css")
                useStylesheet("/_/net.derfruhling.serenity/serenity-core/builtin/serenity.css")

                WithPage {
                    useEntrypoint("test-app")
                }
            }

            Body {
                SlotBody()
            }
        }
    }

    register(IndexPage)
    register(ButtonsPage)
    register(SaveDataPage)
}
