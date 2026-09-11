package net.derfruhling.serenity.channel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.IntState
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.ReusableContent
import androidx.compose.runtime.State
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import net.derfruhling.serenity.HtmlComposable

private val disposeKeys = mutableMapOf<String, MutableMap<Long, () -> Unit>>()

private fun disposeKeysFor(key: String): MutableMap<Long, () -> Unit> {
    return disposeKeys[key] ?: mutableMapOf<Long, () -> Unit>().also { disposeKeys[key] = it }
}

internal fun invalidate(key: String) {
    disposeKeys[key]?.let {
        for(fn in it.values) fn()
    }
}

@Composable
@HtmlComposable
actual fun InvalidateRemotely(key: String, fn: @Composable @HtmlComposable (() -> Unit)) {
    val key by countRemoteInvalidations(key)

    key(key) {
        fn()
    }
}

@Composable
@HtmlComposable
actual fun countRemoteInvalidations(key: String): IntState {
    val hashCode = currentCompositeKeyHashCode
    val mutableState = remember { mutableIntStateOf(0) }

    DisposableEffect(key, hashCode) {
        val map = disposeKeysFor(key)
        map[hashCode] = { mutableState.intValue++ }

        onDispose {
            map.remove(hashCode)
            if(map.isEmpty()) disposeKeys.remove(key)
        }
    }

    return mutableState
}
