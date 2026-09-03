// WITH_STDLIB

import net.derfruhling.serenity.annotations.*

@ClientOnly
fun clientFn() {}

@ServerOnly
fun serverFn() {}

fun box(): String {
    return "OK"
}
