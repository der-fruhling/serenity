package net.derfruhling.serene.wasm

import net.derfruhling.serene.wasm.sections.*

abstract class AbstractModuleVisitor(val base: ModuleVisitor) : ModuleVisitor {
    override fun visitMagic(magic: UInt, version: UInt) = base.visitMagic(magic, version)

    override fun visit(section: Section) {
        when(section) {
            is CodeSection -> visitCodeSection(section)
            is DataCountSection -> visitDataCountSection(section)
            is DataSection -> visitDataSection(section)
            is ElementSection -> visitElementSection(section)
            is ExportSection -> visitExportSection(section)
            is FunctionSection -> visitFunctionSection(section)
            is GlobalSection -> visitGlobalSection(section)
            is ImportSection -> visitImportSection(section)
            is MemorySection -> visitMemorySection(section)
            is StartSection -> visitStartSection(section)
            is TableSection -> visitTableSection(section)
            is TagSection -> visitTagSection(section)
            is TypeSection -> visitTypeSection(section)
            is UnidentifiedCustomSection -> visitCustomSection(section)
            is UnknownSection -> visitUnknownSection(section)
            is CustomSection -> visitCustomSection(section)
        }
    }

    open fun visitCustomSection(section: UnidentifiedCustomSection) = base.visit(section)
    open fun visitCustomSection(section: CustomSection) = base.visit(section)
    open fun visitTypeSection(section: TypeSection) = base.visit(section)
    open fun visitImportSection(section: ImportSection) = base.visit(section)
    open fun visitFunctionSection(section: FunctionSection) = base.visit(section)
    open fun visitTableSection(section: TableSection) = base.visit(section)
    open fun visitMemorySection(section: MemorySection) = base.visit(section)
    open fun visitGlobalSection(section: GlobalSection) = base.visit(section)
    open fun visitExportSection(section: ExportSection) = base.visit(section)
    open fun visitStartSection(section: StartSection) = base.visit(section)
    open fun visitElementSection(section: ElementSection) = base.visit(section)
    open fun visitCodeSection(section: CodeSection) = base.visit(section)
    open fun visitDataSection(section: DataSection) = base.visit(section)
    open fun visitDataCountSection(section: DataCountSection) = base.visit(section)
    open fun visitTagSection(section: TagSection) = base.visit(section)
    open fun visitUnknownSection(section: UnknownSection) = base.visit(section)

    override fun visitEnd() = base.visitEnd()
}
