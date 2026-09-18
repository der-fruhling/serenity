@file:HtmlComposable

package net.derfruhling.serenity

import androidx.compose.runtime.*
import net.derfruhling.serenity.elements.HeadContext
import net.derfruhling.serenity.elements.currentPageLocal
import net.derfruhling.serenity.modularity.extension.Body
import net.derfruhling.serenity.modularity.extension.Head
import net.derfruhling.serenity.modularity.extension.PageExtensionController

@Stable
class PageTemplate(val builder: @Composable TemplateBuilder.() -> Unit) {
    @Composable
    fun BuildPage(state: State<PageHolder<*>?>) {
        PageExtensionController.with {
            val builder = object : TemplateBuilder {
                @Composable
                override fun HeadContext.SlotHead() {
                    val details = remember(state.value) { state.value!!.details }
                    head.executeUntil(Head.Title) { it() }
                    ReusableContent(details) {
                        details.title?.let { title(it) }
                    }

                    head.execute { it() }
                }

                @Composable
                override fun WithPage(fn: @Composable ((PageHolder<*>) -> Unit)) {
                    val page = state.value!!
                    CompositionLocalProvider(currentPageLocal provides page) {
                        ReusableContent(page) {
                            fn(page)
                        }
                    }
                }

                @Composable
                override fun SlotBody() {
                    val page = state.value!!
                    val saveDataManager = remember(page) { SaveDataManager(page) }

                    DisposableEffect(saveDataManager) {
                        onDispose {
                            saveDataManager.save()
                        }
                    }

                    saveDataManager.enter {
                        ReusableContent(page) {
                            CompositionLocalProvider(
                                currentPageLocal provides page
                            ) {
                                body.executeUntil(Body.Content) { it() }
                                page.Main()
                                body.execute { it() }
                            }
                        }
                    }
                }
            }

            builder.builder()
        }
    }
}
