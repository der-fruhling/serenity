package net.derfruhling.serenity

import net.derfruhling.serenity.annotations.ClientOnly
import net.derfruhling.serenity.annotations.PageContractsDsl

internal class ClientInitHalted(status: StatusDetail) : RuntimeException(status.toString())

@PageContractsDsl
@ClientOnly
actual class PageClientInit : PageInit() {
    actual override suspend fun halt(detail: StatusDetail): Nothing {
        throw ClientInitHalted(detail)
    }
}
