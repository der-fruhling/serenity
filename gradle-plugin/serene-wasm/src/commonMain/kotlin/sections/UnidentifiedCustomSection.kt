package net.derfruhling.serene.wasm.sections

import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString
import kotlinx.io.write
import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.module.InvalidModuleDataException

data class UnidentifiedCustomSection(val name: String, val bytes: ByteString) : Section {
    override val id: Byte
        get() = Constants.CUSTOM_SECTION

    override fun encode(out: WasmWriter) {
        out.writeString(name)
        out.writeBytes(bytes)
    }

    constructor(reader: WasmReader) : this(reader.readString(), reader.consume())

    fun <T : CustomSection> parse(factory: CustomSection.Factory<T>): T {
        val buffer = Buffer()
        buffer.write(bytes)
        val value = factory.parseFrom(WasmReader(buffer))

        if(!buffer.exhausted()) {
            throw InvalidModuleDataException("Reading custom section '$name' did not exhaust section buffer")
        }

        return value
    }
}
