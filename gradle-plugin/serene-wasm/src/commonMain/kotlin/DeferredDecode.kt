package net.derfruhling.serene.wasm

import net.derfruhling.serene.wasm.module.InvalidModuleDataException

fun interface DeferredDecode<out T> {
    fun finishDecoding(reader: WasmReader): T
}

inline fun <T, R> DeferredDecode<T>.map(crossinline fn: (T) -> R): DeferredDecode<R> {
    return DeferredDecode { fn(this.finishDecoding(it)) }
}

interface Decode<out T> {
    fun deferredDecode(reader: WasmReader): DeferredDecode<T>?

    fun nestedDecode(reader: WasmReader): DeferredDecode<T>? {
        val peek = reader.peek()
        val decode = deferredDecode(peek) ?: return null
        reader.advanceTo(peek)
        return decode
    }

    fun tryDecode(reader: WasmReader) =
        runCatching {
            val peek = reader.peek()
            val decode = deferredDecode(peek)
                ?: throw InvalidModuleDataException("Could not read using decoder $this")
            reader.advanceTo(peek)
            decode.finishDecoding(reader)
        }

    fun decode(reader: WasmReader): T {
        return tryDecode(reader).getOrThrow()
    }
}
