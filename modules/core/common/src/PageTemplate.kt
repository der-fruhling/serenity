@file:HtmlComposable

package net.derfruhling.serenity

import androidx.compose.runtime.*
import net.derfruhling.serenity.annotations.HtmlComposable
import net.derfruhling.serenity.elements.HeadContext
import net.derfruhling.serenity.elements.currentPageLocal

@Stable
interface TemplateBuilder {
    @Composable
    fun HeadContext.SlotHead()

    @Composable
    fun WithPage(fn: @Composable (PageHolder<*>) -> Unit)

    @Composable
    fun SlotBody()
}

@Stable
class PageTemplate(val builder: @Composable TemplateBuilder.() -> Unit) {
    @Composable
    fun BuildPage(state: State<PageHolder<*>?>) {
        val builder = object : TemplateBuilder {
            @Composable
            override fun HeadContext.SlotHead() {
                val details = remember(state.value) { state.value!!.details }
                ReusableContent(details) {
                    details.title?.let { title(it) }
                }
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
                            page.Main()
                        }
                    }
                }
            }
        }

        builder.builder()
    }
}

expect class SaveDataManager(page: PageHolder<*>) {
    fun save()

    @Composable
    fun enter(fn: @Composable () -> Unit)
}
