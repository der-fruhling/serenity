// RUN_PIPELINE_TILL: FRONTEND

package miaw

import net.derfruhling.serenity.annotations.*

@ClientOnly
fun test() {

}

@ServerOnly
fun main() {
    <!ILLEGAL_SIDE!>test()<!>
}
