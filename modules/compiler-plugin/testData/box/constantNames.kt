// WITH_STDLIB

import net.derfruhling.serenity.localization.n

@kotlin.jvm.JvmInline
value class TestValueClass(val long: Long)

const val NAME = "miaw miaw"

fun box(): String {
    val v = n("test-app/hello")
    val m = n(NAME)
    val t = TestValueClass(7)
    return "OK"
}
