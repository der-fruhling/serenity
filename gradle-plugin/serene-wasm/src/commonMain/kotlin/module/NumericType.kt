package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.Decode
import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.module.Type.SimpleType

sealed interface NumericType : ValueType, Unpackable, Default {
    data object I32 : SimpleType<I32>(Constants.TYPE_I32), NumericType {
        override val size: Int
            get() = 4
    }

    data object I64 : SimpleType<I64>(Constants.TYPE_I64), NumericType {
        override val size: Int
            get() = 8
    }

    data object F32 : SimpleType<F32>(Constants.TYPE_F32), NumericType {
        override val size: Int
            get() = 4
    }

    data object F64 : SimpleType<F64>(Constants.TYPE_F64), NumericType {
        override val size: Int
            get() = 8
    }

    companion object : Decode<NumericType> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<NumericType>? {
            return when(reader.readByte()) {
                Constants.TYPE_I32 -> I32
                Constants.TYPE_I64 -> I64
                Constants.TYPE_F32 -> F32
                Constants.TYPE_F64 -> F64
                else -> null
            }
        }
    }
}
