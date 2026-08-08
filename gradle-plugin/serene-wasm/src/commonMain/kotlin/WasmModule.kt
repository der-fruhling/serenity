package net.derfruhling.serene.wasm

import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.bytestring.ByteString
import kotlinx.io.write
import net.derfruhling.serene.wasm.sections.CodeSection
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

data class WasmModule(val sections: Array<Section>) {
    val typeSection = sections.findOf<TypeSection>() ?: TypeSection.EMPTY
    val importSection = sections.findOf<ImportSection>() ?: ImportSection.EMPTY
    val functionSection = sections.findOf<FunctionSection>() ?: FunctionSection.EMPTY
    val tableSection = sections.findOf<TableSection>() ?: TableSection.EMPTY
    val memorySection = sections.findOf<MemorySection>() ?: MemorySection.EMPTY
    val tagSection = sections.findOf<TagSection>() ?: TagSection.EMPTY
    val globalSection = sections.findOf<GlobalSection>() ?: GlobalSection.EMPTY
    val exportSection = sections.findOf<ExportSection>() ?: ExportSection.EMPTY
    val startSection = sections.findOf<StartSection>()
    val elementSection = sections.findOf<ElementSection>() ?: ElementSection.EMPTY
    val dataCountSection = sections.findOf<DataCountSection>()
    val codeSection = sections.findOf<CodeSection>() ?: CodeSection.EMPTY
    val dataSection = sections.findOf<DataSection>() ?: DataSection.EMPTY

    companion object {
        private inline fun <reified T> Array<*>.findOf() = find { it is T } as T?

        fun parse(from: WasmReader): WasmModule {
            val mutable = MutableWasmModule()
            val parser = WasmParser(from, mutable)
            parser.parseModule()
            return mutable.finish()
        }

        fun parse(bytes: Source) = parse(WasmReader(bytes))
        fun parse(bytes: ByteString) = parse(Buffer().also { it.write(bytes) })
        fun parse(bytes: ByteArray) = parse(Buffer().also { it.write(bytes) })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WasmModule) return false

        if (!sections.contentEquals(other.sections)) return false

        return true
    }

    override fun hashCode(): Int {
        return sections.contentHashCode()
    }

    override fun toString(): String {
        return "WasmModule(${sections.contentToString()})"
    }
}
