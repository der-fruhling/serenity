package net.derfruhling.serene.wasm.instruction

import net.derfruhling.serene.wasm.Decode
import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

sealed interface Catch : Encode {
    val labelIndex: UInt

    data class Tag(val tagIndex: UInt, override val labelIndex: UInt) : Catch {
        override fun encode(out: WasmWriter) {
            out.writeByte(0)
            out.writeUInt(tagIndex)
            out.writeUInt(labelIndex)
        }
    }

    data class Ref(val tagIndex: UInt, override val labelIndex: UInt) : Catch {
        override fun encode(out: WasmWriter) {
            out.writeByte(1)
            out.writeUInt(tagIndex)
            out.writeUInt(labelIndex)
        }
    }

    data class All(override val labelIndex: UInt) : Catch {
        override fun encode(out: WasmWriter) {
            out.writeByte(2)
            out.writeUInt(labelIndex)
        }
    }

    data class AllRef(override val labelIndex: UInt) : Catch {
        override fun encode(out: WasmWriter) {
            out.writeByte(3)
            out.writeUInt(labelIndex)
        }
    }

    companion object : Decode<Catch> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<Catch>? {
            return when(reader.readByte().toInt()) {
                0 -> DeferredDecode { Tag(it.readUInt(), it.readUInt()) }
                1 -> DeferredDecode { Ref(it.readUInt(), it.readUInt()) }
                2 -> DeferredDecode { All(it.readUInt()) }
                3 -> DeferredDecode { AllRef(it.readUInt()) }
                else -> null
            }
        }
    }
}
