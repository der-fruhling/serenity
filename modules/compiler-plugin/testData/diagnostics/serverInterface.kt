// RUN_PIPELINE_TILL: FRONTEND

package miaw

import net.derfruhling.serenity.annotations.*

@ServerOnly
fun test() {

}

fun interface ClientFun {
    @ClientOnly
    fun execute()
}

fun main() {
    val f = ClientFun { <!ILLEGAL_SIDE!>test()<!> }
}
