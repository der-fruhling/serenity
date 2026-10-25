// RUN_PIPELINE_TILL: FRONTEND

package miaw

import net.derfruhling.serenity.annotations.*

@ServerOnly
fun test() {

}

@ClientOnly
fun main() {
    <!ILLEGAL_SIDE!>test()<!>
}
