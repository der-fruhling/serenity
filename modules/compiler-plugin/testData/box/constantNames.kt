// WITH_STDLIB

import net.derfruhling.serenity.localization.n

@kotlin.jvm.JvmInline
value class TestValueClass(val long: Long)

fun box(): String {
    val v = n("test-app/hello")
    val t = TestValueClass(7)
    return "OK"
}
