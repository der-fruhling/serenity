package net.derfruhling.serenity.modularity

import androidx.compose.runtime.ProvidedValue

interface ProvideContext : CommonContext {
    suspend fun use(provide: ProvidedValue<*>)
}
