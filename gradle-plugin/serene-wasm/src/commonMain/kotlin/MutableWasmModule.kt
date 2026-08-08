package net.derfruhling.serene.wasm

import net.derfruhling.serene.wasm.module.InvalidModuleDataException
import net.derfruhling.serene.wasm.sections.CodeSection
import net.derfruhling.serene.wasm.sections.CustomSection
import net.derfruhling.serene.wasm.sections.DataCountSection
import net.derfruhling.serene.wasm.sections.DataSection
import net.derfruhling.serene.wasm.sections.ElementSection
import net.derfruhling.serene.wasm.sections.ExportSection
import net.derfruhling.serene.wasm.sections.FunctionSection
import net.derfruhling.serene.wasm.sections.GlobalSection
import net.derfruhling.serene.wasm.sections.ImportSection
import net.derfruhling.serene.wasm.sections.MemorySection
import net.derfruhling.serene.wasm.sections.Section
import net.derfruhling.serene.wasm.sections.StartSection
import net.derfruhling.serene.wasm.sections.TableSection
import net.derfruhling.serene.wasm.sections.TagSection
import net.derfruhling.serene.wasm.sections.TypeSection
import net.derfruhling.serene.wasm.sections.UnidentifiedCustomSection
import net.derfruhling.serene.wasm.sections.UnknownSection

class MutableWasmModule : ModuleVisitor {
    private enum class State {
        MODULE_START,
        TYPE,
        IMPORT,
        FUNC,
        TABLE,
        MEM,
        TAG,
        GLOBAL,
        EXPORT,
        START,
        ELEM,
        DATA_COUNT,
        CODE,
        DATA
    }

    private var state = State.MODULE_START
    private val sections = mutableListOf<Section>()

    override fun visitMagic(magic: UInt, version: UInt) {
        if (magic != Constants.MAGIC) {
            throw InvalidModuleDataException("Incorrect magic ${magic.toHexString()}, expected ${Constants.MAGIC.toHexString()}")
        }

        if (version != Constants.VERSION) {
            throw InvalidModuleDataException("Mismatched version: module of version $version is incompatible with this library, which only supports modules of version ${Constants.VERSION}")
        }
    }

    override fun visit(section: Section) {
        val newState = when(section) {
            is CodeSection -> State.CODE
            is DataCountSection -> State.DATA_COUNT
            is DataSection -> State.DATA
            is ElementSection -> State.ELEM
            is ExportSection -> State.EXPORT
            is FunctionSection -> State.FUNC
            is GlobalSection -> State.GLOBAL
            is ImportSection -> State.IMPORT
            is MemorySection -> State.MEM
            is StartSection -> State.START
            is TableSection -> State.TABLE
            is TagSection -> State.TAG
            is TypeSection -> State.TYPE
            is CustomSection, is UnidentifiedCustomSection, is UnknownSection -> {
                sections.add(section)
                return
            }
        }

        if(newState <= state) {
            throw InvalidModuleDataException("Cannot place section of type $newState after $state")
        }

        sections.add(section)
    }

    override fun visitEnd() {}

    fun finish() = WasmModule(sections.toTypedArray())
}
