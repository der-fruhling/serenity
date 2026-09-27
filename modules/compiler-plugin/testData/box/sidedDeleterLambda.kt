// WITH_STDLIB

import net.derfruhling.serenity.annotations.*

fun testClient(fn: @ClientOnly Int.(String) -> Unit) {

}

fun testServer(fn: @ServerOnly () -> Unit) {
    fn()
}

fun box(): String {
    testClient {
        println("Hello, $it!")
    }

    testServer {
        println("Hello, server!")
    }

    return "OK"
}
