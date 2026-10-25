// WITH_STDLIB

import net.derfruhling.serenity.annotations.*

fun testClient(fn: @ClientOnly Int.(String) -> Unit) {

}

fun testServer(fn: @ServerOnly () -> Unit) {
    fn()
}

fun interface TestServerOnly {
    @ServerOnly
    fun testFn()
}

fun box(): String {
    testClient {
        println("Hello, $it!")
    }

    testServer {
        println("Hello, server!")
    }

    val testServer = TestServerOnly { println("Oh no!") }

    return "OK"
}
