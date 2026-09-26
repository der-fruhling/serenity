package net.derfruhling.serenity

import androidx.compose.runtime.DisallowComposableCalls
import net.derfruhling.serenity.annotations.Intrinsic

interface PageContract {
    var title: String
}

@Intrinsic
fun pageContract(@Suppress("unused") fn: @DisallowComposableCalls PageContract.() -> Unit) {}
