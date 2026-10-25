@file:OptIn(IntrinsicHasSpecialMeaning::class)

package net.derfruhling.serenity

import androidx.compose.runtime.DisallowComposableCalls
import net.derfruhling.serenity.annotations.ClientOnly
import net.derfruhling.serenity.annotations.Intrinsic
import net.derfruhling.serenity.annotations.IntrinsicHasSpecialMeaning
import net.derfruhling.serenity.annotations.PageContractsDsl
import net.derfruhling.serenity.annotations.PromotedLambda
import net.derfruhling.serenity.annotations.Role
import net.derfruhling.serenity.annotations.ServerOnly
import net.derfruhling.serenity.http.HttpStatus
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface StatusDetail {
    data object Empty : StatusDetail {
        override fun toString(): String {
            return "<no details>"
        }
    }
}

@PageContractsDsl
abstract class PageInit {
    abstract suspend fun halt(detail: StatusDetail = StatusDetail.Empty): Nothing
}

@OptIn(ExperimentalContracts::class)
val PageInit.server: Boolean
    get() {
        contract {
            returns(true) implies (this@server is PageServerInit)
        }

        return this is PageServerInit
    }

@PageContractsDsl
abstract class PageServerInit : PageInit() {
    abstract suspend fun respond(status: HttpStatus, detail: StatusDetail = StatusDetail.Empty): Nothing

    override suspend fun halt(detail: StatusDetail): Nothing =
        respond(HttpStatus.INTERNAL_SERVER_ERROR, detail)
}

@OptIn(ExperimentalContracts::class)
val PageInit.client: Boolean
    get() {
        contract {
            returns(true) implies (this@client is PageClientInit)
        }

        return this is PageClientInit
    }

@Intrinsic(Role.PAGE_INIT)
@OptIn(ExperimentalContracts::class)
fun pageInit(fn: @PromotedLambda @DisallowComposableCalls suspend PageInit.() -> Unit) {
    @Suppress("WRONG_INVOCATION_KIND")
    contract {
        callsInPlace(fn, kind = InvocationKind.EXACTLY_ONCE)
    }

    throw NotImplementedError()
}

@Intrinsic(Role.PAGE_INIT)
@OptIn(ExperimentalContracts::class)
fun serverInit(fn: @PromotedLambda @DisallowComposableCalls @ServerOnly suspend PageServerInit.() -> Unit) {
    @Suppress("WRONG_INVOCATION_KIND")
    contract {
        callsInPlace(fn, kind = InvocationKind.AT_MOST_ONCE)
    }

    throw NotImplementedError()
}

@Intrinsic(Role.PAGE_INIT)
@OptIn(ExperimentalContracts::class)
fun clientInit(fn: @PromotedLambda @DisallowComposableCalls @ClientOnly suspend PageClientInit.() -> Unit) {
    @Suppress("WRONG_INVOCATION_KIND")
    contract {
        callsInPlace(fn, kind = InvocationKind.AT_MOST_ONCE)
    }

    throw NotImplementedError()
}
