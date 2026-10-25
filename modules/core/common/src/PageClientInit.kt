@file:GenerateServerStubs

package net.derfruhling.serenity

import net.derfruhling.serenity.annotations.ClientOnly
import net.derfruhling.serenity.annotations.GenerateServerStubs
import net.derfruhling.serenity.annotations.PageContractsDsl

@PageContractsDsl
@ClientOnly
expect class PageClientInit : PageInit {
    override suspend fun halt(detail: StatusDetail): Nothing
}
