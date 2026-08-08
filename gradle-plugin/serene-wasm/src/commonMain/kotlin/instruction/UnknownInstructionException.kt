package net.derfruhling.serene.wasm.instruction

import net.derfruhling.serene.wasm.module.InvalidModuleDataException

open class UnknownInstructionException : InvalidModuleDataException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}