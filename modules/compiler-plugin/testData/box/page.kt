// WITH_STDLIB

import net.derfruhling.serenity.annotations.*
import net.derfruhling.serenity.pageContract
import net.derfruhling.serenity.PageHolder
import net.derfruhling.serenity.PageHolderFactory
import net.derfruhling.serenity.PageContext
import androidx.compose.runtime.*

@Composable
@Page("/")
fun IndexPage() {
    pageContract {
        title = "Hello, world!"
    }
}

@Composable
@Page("/")
fun Page2(param1: String, param2: Int) {
    pageContract {
        title = "Hello, ${param1}!"
    }
}

class Test {
    companion object Factory {
        fun of() {}
    }
}

inline fun <reified T : PageHolder<T>> accept(factory: PageHolderFactory<PageContext, T>) {

}

fun box(): String {
    val page1 = IndexPage
    val page2 = Page2.of("world", 42)
    Test.of()
    accept(IndexPage)
    accept(Page2)
    return "OK"
}
