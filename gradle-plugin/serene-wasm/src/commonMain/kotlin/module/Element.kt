package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.CodeBlob
import net.derfruhling.serene.wasm.Decode
import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

sealed interface Element : Encode {
    data class Functions(val mode: ElementMode, val functions: List<UInt>) : Element {
        override fun encode(out: WasmWriter) {
            when(mode) {
                is ElementMode.Active -> {
                    if(mode.tableIndex == 0u) {
                        out.writeByte(0)
                    } else {
                        out.writeByte(2)
                        out.writeUInt(mode.tableIndex)
                    }

                    out.writeBytes(mode.offsetExpr.byteString)
                }

                ElementMode.Declarative -> {
                    out.writeByte(3)
                    out.writeByte(0) // elem-kind, only 0 supported for now
                }

                ElementMode.Passive -> {
                    out.writeByte(1)
                    out.writeByte(0) // elem-kind, only 0 supported for now
                }
            }

            out.writeList(functions) { writeUInt(it) }
        }

        companion object {
            fun decode(typeByte: Int, reader: WasmReader): Functions {
                val mode = when(typeByte) {
                    0 -> {
                        val offsetExpr = reader.readExpr()
                        ElementMode.Active(0u, offsetExpr)
                    }

                    1 -> {
                        if(reader.readByte() != 0.toByte())
                            throw InvalidModuleDataException("Invalid element kind")
                        ElementMode.Passive
                    }

                    2 -> {
                        val tableIndex = reader.readUInt()
                        val offsetExpr = reader.readExpr()
                        ElementMode.Active(tableIndex, offsetExpr)
                    }

                    3 -> {
                        if(reader.readByte() != 0.toByte())
                            throw InvalidModuleDataException("Invalid element kind")
                        ElementMode.Declarative
                    }

                    else -> throw InvalidModuleDataException("Incorrect mode type $typeByte")
                }

                return Functions(mode, reader.readList { it.readUInt() })
            }
        }
    }

    data class Expressions(val mode: ElementMode, val type: RefType, val expressions: List<CodeBlob>) : Element {
        override fun encode(out: WasmWriter) {
            when(mode) {
                is ElementMode.Active -> {
                    if(mode.tableIndex == 0u && type == RefType.Nullable(HeapType.Func)) {
                        out.writeByte(4)
                        out.writeBytes(mode.offsetExpr.byteString)
                    } else {
                        out.writeByte(6)
                        out.writeUInt(mode.tableIndex)
                        out.writeBytes(mode.offsetExpr.byteString)
                        type.encode(out)
                    }
                }

                ElementMode.Declarative -> {
                    out.writeByte(7)
                    type.encode(out)
                }

                ElementMode.Passive -> {
                    out.writeByte(5)
                    type.encode(out)
                }
            }

            out.writeList(expressions) { writeBytes(it.byteString) }
        }

        companion object {
            fun decode(typeByte: Int, reader: WasmReader): Expressions {
                val (type, mode) = when(typeByte) {
                    4 -> {
                        val offsetExpr = reader.readExpr()
                        RefType.Nullable(HeapType.Func) to ElementMode.Active(0u, offsetExpr)
                    }

                    5 -> {
                        RefType.decode(reader) to ElementMode.Passive
                    }

                    6 -> {
                        val tableIndex = reader.readUInt()
                        val offsetExpr = reader.readExpr()
                        RefType.decode(reader) to ElementMode.Active(tableIndex, offsetExpr)
                    }

                    7 -> {
                        RefType.decode(reader) to ElementMode.Declarative
                    }

                    else -> throw InvalidModuleDataException("Incorrect mode type $typeByte")
                }

                return Expressions(mode, type, reader.readList { it.readExpr() })
            }
        }
    }

    companion object : Decode<Element> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<Element>? {
            return when(val typeByte = reader.readByte().toInt()) {
                0, 1, 2, 3 -> DeferredDecode { Functions.decode(typeByte, it) }
                4, 5, 6, 7 -> DeferredDecode { Expressions.decode(typeByte, it) }
                else -> null
            }
        }
    }
}
