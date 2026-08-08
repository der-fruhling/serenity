package net.derfruhling.serene.wasm

object Constants {
    const val MAGIC: UInt = 0x0061736Du
    const val VERSION: UInt = 1u

    const val CUSTOM_SECTION: Byte = 0
    const val TYPE_SECTION: Byte = 1
    const val IMPORT_SECTION: Byte = 2
    const val FUNCTION_SECTION: Byte = 3
    const val TABLE_SECTION: Byte = 4
    const val MEMORY_SECTION: Byte = 5
    const val GLOBAL_SECTION: Byte = 6
    const val EXPORT_SECTION: Byte = 7
    const val START_SECTION: Byte = 8
    const val ELEMENT_SECTION: Byte = 9
    const val CODE_SECTION: Byte = 10
    const val DATA_SECTION: Byte = 11
    const val DATA_COUNT_SECTION: Byte = 12
    const val TAG_SECTION: Byte = 13

    const val IS_BUILTIN_TYPE_BIT: Byte = 6

    const val PACK_TYPE_I16: Byte = 0x77
    const val PACK_TYPE_I8: Byte = 0x78

    const val CONST: Byte = 0x00
    const val MUT: Byte = 0x01

    const val COMP_TYPE_ARRAY: Byte = 0x5E
    const val COMP_TYPE_STRUCT: Byte = 0x5F
    const val COMP_TYPE_FUNC: Byte = 0x60

    const val TYPE_V128: Byte = 0x7B
    const val TYPE_F64: Byte = 0x7C
    const val TYPE_F32: Byte = 0x7D
    const val TYPE_I64: Byte = 0x7E
    const val TYPE_I32: Byte = 0x7F

    const val ABS_HEAP_TYPE_EXN: Byte = 0x69
    const val ABS_HEAP_TYPE_ARRAY: Byte = 0x6A
    const val ABS_HEAP_TYPE_STRUCT: Byte = 0x6B
    const val ABS_HEAP_TYPE_I31: Byte = 0x6C
    const val ABS_HEAP_TYPE_EQ: Byte = 0x6D
    const val ABS_HEAP_TYPE_ANY: Byte = 0x6E
    const val ABS_HEAP_TYPE_EXTERN: Byte = 0x6F
    const val ABS_HEAP_TYPE_FUNC: Byte = 0x70
    const val ABS_HEAP_TYPE_NONE: Byte = 0x71
    const val ABS_HEAP_TYPE_NOEXTERN: Byte = 0x72
    const val ABS_HEAP_TYPE_NOFUNC: Byte = 0x73
    const val ABS_HEAP_TYPE_NOEXN: Byte = 0x74

    const val REC_TYPE_COMPOUND: Byte = 0x4E
    const val SUB_TYPE_FINAL: Byte = 0x4F
    const val SUB_TYPE_NON_FINAL: Byte = 0x50

    const val REF_TYPE_NULLABLE: Byte = 0x63
    const val REF_TYPE_NOT_NULL: Byte = 0x64
}
