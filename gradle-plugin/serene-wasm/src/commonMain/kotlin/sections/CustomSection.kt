package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

abstract class CustomSection(val name: String) : Section {
    final override val id: Byte
        get() = Constants.CUSTOM_SECTION

    final override fun encode(out: WasmWriter) {
        out.writeString(name)
        encodeCustom(out)
    }

    protected abstract fun encodeCustom(out: WasmWriter)

    fun interface Factory<T: CustomSection> {
        fun parseFrom(reader: WasmReader): T
    }
}
