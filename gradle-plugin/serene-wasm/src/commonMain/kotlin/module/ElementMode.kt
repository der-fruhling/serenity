package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.CodeBlob

sealed interface ElementMode {
    data class Active(val tableIndex: UInt, val offsetExpr: CodeBlob) : ElementMode
    data object Passive : ElementMode
    data object Declarative : ElementMode
}