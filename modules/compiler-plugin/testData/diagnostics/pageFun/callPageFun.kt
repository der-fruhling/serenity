// RUN_PIPELINE_TILL: FRONTEND

package miaw

import net.derfruhling.serenity.annotations.*
import androidx.compose.runtime.*

@Page("/test")
@Composable
fun test() {

}

fun main() {
    <!CALLED_PAGE_FUN!>test()<!>
}
