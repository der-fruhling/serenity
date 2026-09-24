package net.derfruhling.serenity.compiler.ir

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.ModuleLoweringPass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.FqName

abstract class AbstractSerenityTransformer : IrElementTransformerVoid(),
    ModuleLoweringPass,
    FileLoweringPass {
    protected val serenityPackage by lazy { FqName("net.derfruhling.serenity") }
    protected val localizationPackage by lazy { FqName("net.derfruhling.serenity.localization") }

    protected lateinit var currentFile: IrFile
        private set

    override fun visitFile(declaration: IrFile): IrFile {
        currentFile = declaration
        return super.visitFile(declaration)
    }

    override fun lower(irModule: IrModuleFragment) {
        irModule.transformChildrenVoid(this)
    }

    override fun lower(irFile: IrFile) {
        irFile.transformChildrenVoid(this)
    }
}