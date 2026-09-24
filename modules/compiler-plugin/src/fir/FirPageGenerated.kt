package net.derfruhling.serenity.compiler.fir

import org.jetbrains.kotlin.GeneratedDeclarationKey

sealed class FirPageGenerated : GeneratedDeclarationKey() {
    data object PageClass : FirPageGenerated()
    data object PageFactoryClass : FirPageGenerated()
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
}