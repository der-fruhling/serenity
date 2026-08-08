package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.Decode
import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

sealed interface CompositeType : Type, RecursiveType.SubType {
    data class Array(val type: FieldType) : CompositeType {
        override fun encode(out: WasmWriter) {
            out.writeByte(Constants.COMP_TYPE_ARRAY)
            type.encode(out)
        }
    }

    data class Struct(val fields: List<FieldType>) : CompositeType {
        override fun encode(out: WasmWriter) {
            out.writeByte(Constants.COMP_TYPE_STRUCT)
            out.writeList(fields)
        }
    }

    data class Func(val args: ResultType, val result: ResultType) : CompositeType {
        override fun encode(out: WasmWriter) {
            out.writeByte(Constants.COMP_TYPE_FUNC)
            args.encode(out)
            result.encode(out)
        }
    }

    companion object : Decode<CompositeType> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<CompositeType>? {
            return when(reader.readByte()) {
                Constants.COMP_TYPE_ARRAY -> DeferredDecode {
                    Array(FieldType(it))
                }

                Constants.COMP_TYPE_STRUCT -> DeferredDecode {
                    Struct(it.readList(::FieldType))
                }

                Constants.COMP_TYPE_FUNC -> DeferredDecode {
                    Func(ResultType(it), ResultType(it))
                }

                else -> null
            }
        }
    }
}
