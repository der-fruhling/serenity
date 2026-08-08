package net.derfruhling.serene.wasm.tests

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.derfruhling.serene.wasm.WasmModule
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.WasmWriterModuleVisitor
import kotlin.test.Test

class ModuleTest {
    @Test
    fun `test parse`() {
        val module = SystemFileSystem.source(Path("test-cases", "miaw.wasm")).use {
            WasmModule.parse(it.buffered())
        }

        println(module)

        SystemFileSystem.sink(Path("test-cases", "miaw2.wasm")).buffered().use {
            val visitor = WasmWriterModuleVisitor(WasmWriter(it))
            visitor.visit(module)
        }
    }
}
