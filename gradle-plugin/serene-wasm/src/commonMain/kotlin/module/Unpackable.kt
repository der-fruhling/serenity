package net.derfruhling.serene.wasm.module

sealed interface Unpackable : ValueType {
    val size: Int
}