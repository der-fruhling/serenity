// RUN_PIPELINE_TILL: FRONTEND

package miaw

import net.derfruhling.serenity.annotations.*
import net.derfruhling.serenity.pageContract
import net.derfruhling.serenity.pageInit
import net.derfruhling.serenity.serverInit
import net.derfruhling.serenity.clientInit
import androidx.compose.runtime.*

@Page("/test")
@Composable
fun wellFormed() {
    pageInit {}
    serverInit {}
    clientInit {}

    pageContract {}

    println("Yep!")
}

@Page("/test")
@Composable
fun misplacedBody() {
    pageInit {}
    serverInit {}
    clientInit {}

    println("Nope!")

    <!MISPLACED_PAGE_CONTRACT!>pageContract {}<!>
}

@Page("/test")
@Composable
fun misplacedInit() {
    pageContract {}

    <!MISPLACED_PAGE_CONTRACT!>pageInit {}<!>
    <!MISPLACED_PAGE_CONTRACT!>serverInit {}<!>
    <!MISPLACED_PAGE_CONTRACT!>clientInit {}<!>

    println("Nope!")
}
