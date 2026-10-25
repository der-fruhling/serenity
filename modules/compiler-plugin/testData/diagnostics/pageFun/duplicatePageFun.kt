// RUN_PIPELINE_TILL: FRONTEND

package miaw

import net.derfruhling.serenity.annotations.*
import androidx.compose.runtime.*

@Page("/test")
@Composable
fun <!DUPLICATE_PAGE!>test<!>(param1: Int) {

}

@Page("/test")
@Composable
fun <!DUPLICATE_PAGE!>test<!>(param2: Long) {

}
