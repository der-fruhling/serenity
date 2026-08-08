package net.derfruhling.serene.wasm.instruction

fun interface OpUsage {
    fun OpUsageContext.validate()

    companion object {
        val NULL = OpUsage {}
    }
}
