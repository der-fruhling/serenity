package net.derfruhling.serenity.compiler.fir

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId

sealed class FirPageGenerated : GeneratedDeclarationKey() {
    data class PageClass(val isSubClass: Boolean) : FirPageGenerated()
    data object PageFactoryClass : FirPageGenerated()
    data object PageSerializerClass : FirPageGenerated()
    data object PageIdProperty : FirPageGenerated()
    data object PagePathProperty : FirPageGenerated()
    data object PageDetailsProperty : FirPageGenerated()
    data object PageExtensionsProperty : FirPageGenerated()
    data object PageMainFun : FirPageGenerated()
    data object PageFactoryCreateFun : FirPageGenerated()
    data object PageFactoryOfFun : FirPageGenerated()
    data object Parameter : FirPageGenerated()
    data object ObjectConstructor : FirPageGenerated()
    data object PageConstructor : FirPageGenerated()
    data object PageFactoryProperty : FirPageGenerated()
    data object PageSerializerGetter : FirPageGenerated()
    data object PageSerializeFun : FirPageGenerated()
    data object PageDeserializeFun : FirPageGenerated()
    data object PageDescriptorProperty : FirPageGenerated()
    data class PageEntrypointFun(val callableId: CallableId) : FirPageGenerated()
}