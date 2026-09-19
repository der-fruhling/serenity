package net.derfruhling.serenity.channel

import androidx.compose.runtime.*
import net.derfruhling.serenity.HtmlComposable

private val disposeKeys = mutableMapOf<String, MutableMap<Long, () -> Unit>>()

private fun disposeKeysFor(key: String): MutableMap<Long, () -> Unit> {
    return disposeKeys[key] ?: mutableMapOf<Long, () -> Unit>().also { disposeKeys[key] = it }
}

internal fun invalidate(key: String) {
    disposeKeys[key]?.let {
        for (fn in it.values) fn()
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
            if (map.isEmpty()) disposeKeys.remove(key)
        }
    }

    return mutableState
}
