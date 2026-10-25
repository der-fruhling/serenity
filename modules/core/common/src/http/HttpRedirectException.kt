package net.derfruhling.serenity.http

interface ThrowRedirects {
    val targetUrl: String
}

sealed class HttpRedirectException(status: HttpStatus, override val targetUrl: String) :
    HttpStatusException(status),
    ThrowRedirects {
    open class MovedPermanently(targetUrl: String) : HttpRedirectException(
        HttpStatus.MOVED_PERMANENTLY,
        targetUrl
    )

    open class Found(targetUrl: String) : HttpRedirectException(HttpStatus.FOUND, targetUrl)
    open class SeeOther(targetUrl: String) : HttpRedirectException(HttpStatus.SEE_OTHER, targetUrl)
    open class Temporary(targetUrl: String) : HttpRedirectException(HttpStatus.TEMPORARY_REDIRECT, targetUrl)
    open class Permanent(targetUrl: String) : HttpRedirectException(HttpStatus.PERMANENT_REDIRECT, targetUrl)
}
