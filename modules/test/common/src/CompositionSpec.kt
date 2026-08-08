package net.derfruhling.serenity.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.withRunningRecomposer
import io.kotest.common.KotestInternal
import io.kotest.core.names.TestNameBuilder
import io.kotest.core.spec.AbstractSpec
import io.kotest.core.spec.TestDefinitionBuilder
import io.kotest.core.spec.style.TestRunnable
import io.kotest.core.test.TestScope
import io.kotest.core.test.TestType
import io.kotest.core.test.config.TestConfig
import kotlinx.coroutines.withContext
import net.derfruhling.serenity.tree.HtmlCompositionContext
import net.derfruhling.serenity.tree.composeHtmlOnce
import net.derfruhling.serenity.tree.platform.DocumentFragment
import kotlin.jvm.JvmInline

@OptIn(KotestInternal::class)
abstract class CompositionSpec() : AbstractSpec() {
    constructor(fn: CompositionSpec.() -> Unit) : this() {
        fn()
    }

    @TestRunnable
    operator fun String.invoke(f: TestScope.() -> Unit) {
        add(
            TestDefinitionBuilder.builder(
                TestNameBuilder.builder(this).build(),
                TestType.Test
            ).withConfig(TestConfig(blockingTest = true)).build(f)
        )
    }

    @TestRunnable
    infix fun String.suspends(f: suspend TestScope.() -> Unit) {
        add(
            TestDefinitionBuilder.builder(
                TestNameBuilder.builder(this).build(),
                TestType.Test
            ).withConfig(TestConfig(coroutineTestScope = true)).build(f)
        )
    }

    suspend fun TestScope.runStaticComposeTest(
        fn: @Composable TestScope.() -> Unit
    ): DocumentFragment {
        return withFrameClock {
            val recomposer = Recomposer(coroutineContext)

            try {
                val fragment = context(HtmlCompositionContext(recomposer)) {
                    composeHtmlOnce { fn() }
                }

                fragment
            } finally {
                recomposer.close()
                recomposer.join()
            }
        }
    }

    suspend fun TestScope.runComposeTest(
        fn: @Composable TestScope.() -> Unit,
        after: TestScope.(DocumentFragment) -> Unit = {}
    ) {
        withFrameClock {
            val fragment = withRunningRecomposer {
                context(HtmlCompositionContext(it)) {
                    composeHtmlOnce { fn() }
                }
            }

            after(fragment)
        }
    }

    @TestRunnable
    fun group(name: String, test: suspend TestScope.() -> Unit) {
        add(
            TestDefinitionBuilder.builder(
                TestNameBuilder.builder(name).build(),
                TestType.Container
            ).build { CompositionTestScope(this).test() }
        )
    }
}

internal expect suspend inline fun <T> withFrameClock(crossinline fn: suspend () -> T): T
