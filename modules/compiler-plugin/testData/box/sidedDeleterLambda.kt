// WITH_STDLIB

import net.derfruhling.serenity.annotations.*

fun testClient(fn: @ClientOnly () -> Unit) {

}

fun testServer(fn: @ServerOnly () -> Unit) {
    fn()
}

fun box(): String {
    testClient {
        println("Hello, client!")
    }

    testServer {
        println("Hello, server!")
    }

    return "OK"
}
