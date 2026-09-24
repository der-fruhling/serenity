package net.derfruhling.serenity.compiler.ir

import com.intellij.psi.DummyHolderViewProvider
import org.jetbrains.kotlin.KtPsiSourceFile
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.ModuleLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.fileParent
import org.jetbrains.kotlin.descriptors.impl.MutablePackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.impl.PackageFragmentDescriptorImpl
import org.jetbrains.kotlin.ir.AbstractIrFileEntry
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.PsiIrFileEntry
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.declarations.packageFragmentDescriptor
import org.jetbrains.kotlin.ir.symbols.impl.IrFileSymbolImpl
import org.jetbrains.kotlin.ir.util.NaiveSourceBasedFileEntryImpl
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.addFile
import org.jetbrains.kotlin.ir.util.fileEntry
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrVisitor
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile

interface FileFinder {
    fun withFile(declaration: IrDeclarationBase, fn: IrVisitorVoid.() -> Unit)
}

abstract class AbstractSerenityGenerator(protected val context: IrPluginContext) : IrVisitor<Unit, FileFinder>(),
    ModuleLoweringPass,
    FileLoweringPass {
    protected val serenityPackage by lazy { FqName("net.derfruhling.serenity") }
    protected val localizationPackage by lazy { FqName("net.derfruhling.serenity.localization") }

    override fun visitElement(element: IrElement, data: FileFinder) {
        element.acceptChildren(this, data)
    }

    override fun visitDeclaration(declaration: IrDeclarationBase, data: FileFinder) {
        declaration.acceptChildren(this, data)
    }

    override fun lower(irModule: IrModuleFragment) {
        val deferredFiles = mutableListOf<IrFile>()
        val visitor = object : FileFinder {
            val files = mutableMapOf<IrPackageFragment, IrVisitorVoid>()

            override fun withFile(declaration: IrDeclarationBase, fn: IrVisitorVoid.() -> Unit) {
                val packageFragment = declaration.getPackageFragment()
                val file = files.getOrPut(packageFragment) {
                    val file = IrFileImpl(declaration.fileParent.fileEntry, IrFileSymbolImpl(MutablePackageFragmentDescriptor(irModule.descriptor, packageFragment.packageFqName)), packageFragment.packageFqName, irModule).also {
                        it.module = irModule
                        deferredFiles.add(it)
                    }

                    FileFinderVisitor(file)
                }

                file.fn()
            }
        }

        irModule.acceptChildren(this, visitor)
        irModule.files.addAll(deferredFiles)
    }

    class FileFinderVisitor(val irFile: IrFile) : IrVisitorVoid(), FileFinder {
        override fun visitDeclaration(declaration: IrDeclarationBase) {
            irFile.addChild(declaration)
        }

        override fun withFile(declaration: IrDeclarationBase, fn: IrVisitorVoid.() -> Unit) {
            fn()
        }
    }

    override fun lower(irFile: IrFile) {
        val visitor = FileFinderVisitor(irFile)

        irFile.acceptChildren(this, visitor)
    }
}