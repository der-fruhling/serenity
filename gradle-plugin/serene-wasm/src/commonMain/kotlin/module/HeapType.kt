package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.*
import net.derfruhling.serene.wasm.module.Type.SimpleType

sealed interface HeapType : Type {
    data object Exn : SimpleType<Exn>(Constants.ABS_HEAP_TYPE_EXN), Abstract
    data object Array : SimpleType<Array>(Constants.ABS_HEAP_TYPE_ARRAY), Abstract
    data object Struct : SimpleType<Struct>(Constants.ABS_HEAP_TYPE_STRUCT), Abstract
    data object I31 : SimpleType<I31>(Constants.ABS_HEAP_TYPE_I31), Abstract
    data object Eq : SimpleType<Eq>(Constants.ABS_HEAP_TYPE_EQ), Abstract
    data object Any : SimpleType<Any>(Constants.ABS_HEAP_TYPE_ANY), Abstract
    data object Extern : SimpleType<Extern>(Constants.ABS_HEAP_TYPE_EXTERN), Abstract
    data object Func : SimpleType<Func>(Constants.ABS_HEAP_TYPE_FUNC), Abstract
    data object None : SimpleType<None>(Constants.ABS_HEAP_TYPE_NONE), Abstract
    data object NoExtern : SimpleType<NoExtern>(Constants.ABS_HEAP_TYPE_NOEXTERN), Abstract
    data object NoFunc : SimpleType<NoFunc>(Constants.ABS_HEAP_TYPE_NOFUNC), Abstract
    data object NoExn : SimpleType<NoExn>(Constants.ABS_HEAP_TYPE_NOEXN), Abstract

    data class ByIndex(val index: Int) : Type, HeapType, DeferredDecode<ByIndex> {
        override fun encode(out: WasmWriter) {
            out.writeInt(index)
        }

        override fun finishDecoding(reader: WasmReader): ByIndex {
            return this
        }
    }

    sealed interface Abstract : HeapType {
        companion object : Decode<Abstract> {
            fun fromByte(byte: Byte): Abstract? {
                return when (byte) {
                    Constants.ABS_HEAP_TYPE_EXN -> Exn
                    Constants.ABS_HEAP_TYPE_ARRAY -> Array
                    Constants.ABS_HEAP_TYPE_STRUCT -> Struct
                    Constants.ABS_HEAP_TYPE_I31 -> I31
                    Constants.ABS_HEAP_TYPE_EQ -> Eq
                    Constants.ABS_HEAP_TYPE_ANY -> Any
                    Constants.ABS_HEAP_TYPE_EXTERN -> Extern
                    Constants.ABS_HEAP_TYPE_FUNC -> Func
                    Constants.ABS_HEAP_TYPE_NONE -> None
                    Constants.ABS_HEAP_TYPE_NOEXTERN -> NoExtern
                    Constants.ABS_HEAP_TYPE_NOFUNC -> NoFunc
                    Constants.ABS_HEAP_TYPE_NOEXN -> NoExn
                    else -> null
                }
            }

            override fun deferredDecode(reader: WasmReader): DeferredDecode<Abstract>? {
                val i = reader.readByte()
                @Suppress("UNCHECKED_CAST")
                return fromByte(i) as DeferredDecode<Abstract>?
            }
        }
    }

    companion object : Decode<HeapType> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<HeapType>? {
            val i = reader.readInt()

            @Suppress("UNCHECKED_CAST")
            return if (i < 0) Abstract.fromByte(i.toByte().fixByte()) as DeferredDecode<HeapType>?
            else ByIndex(i)
        }
    }
}
