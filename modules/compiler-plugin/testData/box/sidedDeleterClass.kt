// WITH_STDLIB

import net.derfruhling.serenity.annotations.*

@ClientOnly
class ClientClass {
    fun test() {
        println("Hello, client!")
    }
}

fun box(): String {
    return "OK"
}
