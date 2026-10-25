package net.derfruhling.serenity

import net.derfruhling.serenity.HttpStatus.*
import net.derfruhling.serenity.HttpStatusException.*

enum class HttpErrorKind {
    CLIENT,
    SERVER
}

enum class HttpStatus(
    val code: Int,
    val isRedirection: Boolean = false,
    val isOk: Boolean = isRedirection,
    val error: HttpErrorKind? = null
) {
    CONTINUE(100),
    SWITCHING_PROTOCOLS(101),
    EARLY_HINTS(103),

    OK(200, isOk = true),
    CREATED(201, isOk = true),
    ACCEPTED(202, isOk = true),
    NON_AUTHORITATIVE_INFORMATION(203, isOk = true),
    NO_CONTENT(204, isOk = true),
    RESET_CONTENT(205, isOk = true),
    PARTIAL_CONTENT(206, isOk = true),

    MULTIPLE_CHOICES(300, isRedirection = true),
    MOVED_PERMANENTLY(301, isRedirection = true),
    FOUND(302, isRedirection = true),
    SEE_OTHER(303, isRedirection = true),
    NOT_MODIFIED(304, isRedirection = true),
    TEMPORARY_REDIRECT(307, isRedirection = true),
    PERMANENT_REDIRECT(308, isRedirection = true),

    BAD_REQUEST(400, error = HttpErrorKind.CLIENT),
    UNAUTHORIZED(401, error = HttpErrorKind.CLIENT),
    PAYMENT_REQUIRED(402, error = HttpErrorKind.CLIENT),
    FORBIDDEN(403, error = HttpErrorKind.CLIENT),
    NOT_FOUND(404, error = HttpErrorKind.CLIENT),
    METHOD_NOT_ALLOWED(405, error = HttpErrorKind.CLIENT),
    NOT_ACCEPTABLE(406, error = HttpErrorKind.CLIENT),
    CONFLICT(409, error = HttpErrorKind.CLIENT),
    GONE(410, error = HttpErrorKind.CLIENT),
    LENGTH_REQUIRED(411, error = HttpErrorKind.CLIENT),
    PRECONDITION_FAILED(412, error = HttpErrorKind.CLIENT),
    CONTENT_TOO_LARGE(413, error = HttpErrorKind.CLIENT),
    URI_TOO_LONG(414, error = HttpErrorKind.CLIENT),
    UNSUPPORTED_MEDIA_TYPE(415, error = HttpErrorKind.CLIENT),
    RANGE_NOT_SATISFIABLE(416, error = HttpErrorKind.CLIENT),
    EXPECTATION_FAILED(417, error = HttpErrorKind.CLIENT),
    IM_A_TEAPOT(418, error = HttpErrorKind.CLIENT),
    UPGRADE_REQUIRED(426, error = HttpErrorKind.CLIENT),
    PRECONDITION_REQUIRED(428, error = HttpErrorKind.CLIENT),
    TOO_MANY_REQUESTS(429, error = HttpErrorKind.CLIENT),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, error = HttpErrorKind.CLIENT),

    INTERNAL_SERVER_ERROR(500, error = HttpErrorKind.SERVER),
    NOT_IMPLEMENTED(501, error = HttpErrorKind.SERVER),
    BAD_GATEWAY(502, error = HttpErrorKind.SERVER),
    SERVICE_UNAVAILABLE(503, error = HttpErrorKind.SERVER),
    GATEWAY_TIMEOUT(504, error = HttpErrorKind.SERVER),
    VARIANT_ALSO_NEGOTIATES(506, error = HttpErrorKind.SERVER),
    INSUFFICIENT_STORAGE(507, error = HttpErrorKind.SERVER),
    LOOP_DETECTED(508, error = HttpErrorKind.SERVER),
    NOT_EXTENDED(510, error = HttpErrorKind.SERVER),
    NETWORK_AUTHENTICATION_REQUIRED(511, error = HttpErrorKind.SERVER),
}

open class HttpStatusException : RuntimeException {
    val status: HttpStatus

    constructor(status: HttpStatus) : super() {
        this.status = status
    }

    constructor(status: HttpStatus, message: String?) : super(message) {
        this.status = status
    }

    constructor(status: HttpStatus, message: String?, cause: Throwable?) : super(message, cause) {
        this.status = status
    }

    constructor(status: HttpStatus, cause: Throwable?) : super(cause) {
        this.status = status
    }

    open class BadRequest : HttpStatusException {
        constructor() : super(BAD_REQUEST)
        constructor(message: String?) : super(BAD_REQUEST, message)
        constructor(message: String?, cause: Throwable?) : super(BAD_REQUEST, message, cause)
        constructor(cause: Throwable?) : super(BAD_REQUEST, cause)
    }

    open class Unauthorized : HttpStatusException {
        constructor() : super(UNAUTHORIZED)
        constructor(message: String?) : super(UNAUTHORIZED, message)
        constructor(message: String?, cause: Throwable?) : super(UNAUTHORIZED, message, cause)
        constructor(cause: Throwable?) : super(UNAUTHORIZED, cause)
    }

    open class PaymentRequired : HttpStatusException {
        constructor() : super(PAYMENT_REQUIRED)
        constructor(message: String?) : super(PAYMENT_REQUIRED, message)
        constructor(message: String?, cause: Throwable?) : super(PAYMENT_REQUIRED, message, cause)
        constructor(cause: Throwable?) : super(PAYMENT_REQUIRED, cause)
    }

    open class Forbidden : HttpStatusException {
        constructor() : super(FORBIDDEN)
        constructor(message: String?) : super(FORBIDDEN, message)
        constructor(message: String?, cause: Throwable?) : super(FORBIDDEN, message, cause)
        constructor(cause: Throwable?) : super(FORBIDDEN, cause)
    }

    open class NotFound : HttpStatusException {
        constructor() : super(NOT_FOUND)
        constructor(message: String?) : super(NOT_FOUND, message)
        constructor(message: String?, cause: Throwable?) : super(NOT_FOUND, message, cause)
        constructor(cause: Throwable?) : super(NOT_FOUND, cause)
    }

    open class NotAcceptable : HttpStatusException {
        constructor() : super(NOT_ACCEPTABLE)
        constructor(message: String?) : super(NOT_ACCEPTABLE, message)
        constructor(message: String?, cause: Throwable?) : super(NOT_ACCEPTABLE, message, cause)
        constructor(cause: Throwable?) : super(NOT_ACCEPTABLE, cause)
    }

    open class Conflict : HttpStatusException {
        constructor() : super(CONFLICT)
        constructor(message: String?) : super(CONFLICT, message)
        constructor(message: String?, cause: Throwable?) : super(CONFLICT, message, cause)
        constructor(cause: Throwable?) : super(CONFLICT, cause)
    }

    open class Gone : HttpStatusException {
        constructor() : super(GONE)
        constructor(message: String?) : super(GONE, message)
        constructor(message: String?, cause: Throwable?) : super(GONE, message, cause)
        constructor(cause: Throwable?) : super(GONE, cause)
    }

    open class LengthRequired : HttpStatusException {
        constructor() : super(LENGTH_REQUIRED)
        constructor(message: String?) : super(LENGTH_REQUIRED, message)
        constructor(message: String?, cause: Throwable?) : super(LENGTH_REQUIRED, message, cause)
        constructor(cause: Throwable?) : super(LENGTH_REQUIRED, cause)
    }

    open class PreconditionFailed : HttpStatusException {
        constructor() : super(PRECONDITION_FAILED)
        constructor(message: String?) : super(PRECONDITION_FAILED, message)
        constructor(message: String?, cause: Throwable?) : super(
            PRECONDITION_FAILED,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(PRECONDITION_FAILED, cause)
    }

    open class ContentTooLarge : HttpStatusException {
        constructor() : super(CONTENT_TOO_LARGE)
        constructor(message: String?) : super(CONTENT_TOO_LARGE, message)
        constructor(message: String?, cause: Throwable?) : super(CONTENT_TOO_LARGE, message, cause)
        constructor(cause: Throwable?) : super(CONTENT_TOO_LARGE, cause)
    }

    open class URITooLong : HttpStatusException {
        constructor() : super(URI_TOO_LONG)
        constructor(message: String?) : super(URI_TOO_LONG, message)
        constructor(message: String?, cause: Throwable?) : super(URI_TOO_LONG, message, cause)
        constructor(cause: Throwable?) : super(URI_TOO_LONG, cause)
    }

    open class UnsupportedMediaType : HttpStatusException {
        constructor() : super(UNSUPPORTED_MEDIA_TYPE)
        constructor(message: String?) : super(UNSUPPORTED_MEDIA_TYPE, message)
        constructor(message: String?, cause: Throwable?) : super(
            UNSUPPORTED_MEDIA_TYPE,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(UNSUPPORTED_MEDIA_TYPE, cause)
    }

    open class RangeNotSatisfiable : HttpStatusException {
        constructor() : super(RANGE_NOT_SATISFIABLE)
        constructor(message: String?) : super(RANGE_NOT_SATISFIABLE, message)
        constructor(message: String?, cause: Throwable?) : super(
            RANGE_NOT_SATISFIABLE,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(RANGE_NOT_SATISFIABLE, cause)
    }

    open class ExpectationFailed : HttpStatusException {
        constructor() : super(EXPECTATION_FAILED)
        constructor(message: String?) : super(EXPECTATION_FAILED, message)
        constructor(message: String?, cause: Throwable?) : super(EXPECTATION_FAILED, message, cause)
        constructor(cause: Throwable?) : super(EXPECTATION_FAILED, cause)
    }

    open class ImATeapot : HttpStatusException {
        constructor() : super(IM_A_TEAPOT)
        constructor(message: String?) : super(IM_A_TEAPOT, message)
        constructor(message: String?, cause: Throwable?) : super(IM_A_TEAPOT, message, cause)
        constructor(cause: Throwable?) : super(IM_A_TEAPOT, cause)
    }

    open class PreconditionRequired : HttpStatusException {
        constructor() : super(PRECONDITION_REQUIRED)
        constructor(message: String?) : super(PRECONDITION_REQUIRED, message)
        constructor(message: String?, cause: Throwable?) : super(
            PRECONDITION_REQUIRED,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(PRECONDITION_REQUIRED, cause)
    }

    open class TooManyRequests : HttpStatusException {
        constructor() : super(TOO_MANY_REQUESTS)
        constructor(message: String?) : super(TOO_MANY_REQUESTS, message)
        constructor(message: String?, cause: Throwable?) : super(TOO_MANY_REQUESTS, message, cause)
        constructor(cause: Throwable?) : super(TOO_MANY_REQUESTS, cause)
    }

    open class UnavailableForLegalReasons : HttpStatusException {
        constructor() : super(UNAVAILABLE_FOR_LEGAL_REASONS)
        constructor(message: String?) : super(UNAVAILABLE_FOR_LEGAL_REASONS, message)
        constructor(message: String?, cause: Throwable?) : super(
            UNAVAILABLE_FOR_LEGAL_REASONS,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(UNAVAILABLE_FOR_LEGAL_REASONS, cause)
    }

    open class BadGateway : HttpStatusException {
        constructor() : super(BAD_GATEWAY)
        constructor(message: String?) : super(BAD_GATEWAY, message)
        constructor(message: String?, cause: Throwable?) : super(BAD_GATEWAY, message, cause)
        constructor(cause: Throwable?) : super(BAD_GATEWAY, cause)
    }

    open class ServiceUnavailable : HttpStatusException {
        constructor() : super(SERVICE_UNAVAILABLE)
        constructor(message: String?) : super(SERVICE_UNAVAILABLE, message)
        constructor(message: String?, cause: Throwable?) : super(
            SERVICE_UNAVAILABLE,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(SERVICE_UNAVAILABLE, cause)
    }

    open class GatewayTimeout : HttpStatusException {
        constructor() : super(GATEWAY_TIMEOUT)
        constructor(message: String?) : super(GATEWAY_TIMEOUT, message)
        constructor(message: String?, cause: Throwable?) : super(GATEWAY_TIMEOUT, message, cause)
        constructor(cause: Throwable?) : super(GATEWAY_TIMEOUT, cause)
    }

    open class VariantAlsoNegotiates : HttpStatusException {
        constructor() : super(VARIANT_ALSO_NEGOTIATES)
        constructor(message: String?) : super(VARIANT_ALSO_NEGOTIATES, message)
        constructor(message: String?, cause: Throwable?) : super(
            VARIANT_ALSO_NEGOTIATES,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(VARIANT_ALSO_NEGOTIATES, cause)
    }

    open class InsufficientStorage : HttpStatusException {
        constructor() : super(INSUFFICIENT_STORAGE)
        constructor(message: String?) : super(INSUFFICIENT_STORAGE, message)
        constructor(message: String?, cause: Throwable?) : super(
            INSUFFICIENT_STORAGE,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(INSUFFICIENT_STORAGE, cause)
    }

    open class LoopDetected : HttpStatusException {
        constructor() : super(LOOP_DETECTED)
        constructor(message: String?) : super(LOOP_DETECTED, message)
        constructor(message: String?, cause: Throwable?) : super(LOOP_DETECTED, message, cause)
        constructor(cause: Throwable?) : super(LOOP_DETECTED, cause)
    }
}

interface ThrowRedirects {
    val targetUrl: String
}

sealed class HttpRedirectException(status: HttpStatus, override val targetUrl: String) :
    HttpStatusException(status),
    ThrowRedirects {
    open class MovedPermanently(targetUrl: String) : HttpRedirectException(
        MOVED_PERMANENTLY,
        targetUrl
    )

    open class Found(targetUrl: String) : HttpRedirectException(FOUND, targetUrl)
    open class SeeOther(targetUrl: String) : HttpRedirectException(SEE_OTHER, targetUrl)
    open class Temporary(targetUrl: String) : HttpRedirectException(TEMPORARY_REDIRECT, targetUrl)
    open class Permanent(targetUrl: String) : HttpRedirectException(PERMANENT_REDIRECT, targetUrl)
}

fun badRequest() = BadRequest()
fun badRequest(message: String) = BadRequest(message)
fun badRequest(message: String, cause: Throwable) = BadRequest(message, cause)
fun badRequest(cause: Throwable) = BadRequest(cause)
fun unauthorized() = Unauthorized()
fun unauthorized(message: String) = Unauthorized(message)
fun unauthorized(message: String, cause: Throwable) = Unauthorized(message, cause)
fun unauthorized(cause: Throwable) = Unauthorized(cause)
fun paymentRequired() = PaymentRequired()
fun paymentRequired(message: String) = PaymentRequired(message)
fun paymentRequired(message: String, cause: Throwable) = PaymentRequired(message, cause)
fun paymentRequired(cause: Throwable) = PaymentRequired(cause)
fun forbidden() = Forbidden()
fun forbidden(message: String) = Forbidden(message)
fun forbidden(message: String, cause: Throwable) = Forbidden(message, cause)
fun forbidden(cause: Throwable) = Forbidden(cause)
fun notFound() = NotFound()
fun notFound(message: String) = NotFound(message)
fun notFound(message: String, cause: Throwable) = NotFound(message, cause)
fun notFound(cause: Throwable) = NotFound(cause)
fun notAcceptable() = NotAcceptable()
fun notAcceptable(message: String) = NotAcceptable(message)
fun notAcceptable(message: String, cause: Throwable) = NotAcceptable(message, cause)
fun notAcceptable(cause: Throwable) = NotAcceptable(cause)
fun conflict() = Conflict()
fun conflict(message: String) = Conflict(message)
fun conflict(message: String, cause: Throwable) = Conflict(message, cause)
fun conflict(cause: Throwable) = Conflict(cause)
fun gone() = Gone()
fun gone(message: String) = Gone(message)
fun gone(message: String, cause: Throwable) = Gone(message, cause)
fun gone(cause: Throwable) = Gone(cause)
fun lengthRequired() = LengthRequired()
fun lengthRequired(message: String) = LengthRequired(message)
fun lengthRequired(message: String, cause: Throwable) = LengthRequired(message, cause)
fun lengthRequired(cause: Throwable) = LengthRequired(cause)
fun preconditionFailed() = PreconditionFailed()
fun preconditionFailed(message: String) = PreconditionFailed(message)
fun preconditionFailed(message: String, cause: Throwable) = PreconditionFailed(message, cause)
fun preconditionFailed(cause: Throwable) = PreconditionFailed(cause)
fun contentTooLarge() = ContentTooLarge()
fun contentTooLarge(message: String) = ContentTooLarge(message)
fun contentTooLarge(message: String, cause: Throwable) = ContentTooLarge(message, cause)
fun contentTooLarge(cause: Throwable) = ContentTooLarge(cause)
fun uriTooLong() = URITooLong()
fun uriTooLong(message: String) = URITooLong(message)
fun uriTooLong(message: String, cause: Throwable) = URITooLong(message, cause)
fun uriTooLong(cause: Throwable) = URITooLong(cause)
fun unsupportedMediaType() = UnsupportedMediaType()
fun unsupportedMediaType(message: String) = UnsupportedMediaType(message)
fun unsupportedMediaType(message: String, cause: Throwable) = UnsupportedMediaType(message, cause)
fun unsupportedMediaType(cause: Throwable) = UnsupportedMediaType(cause)
fun rangeNotSatisfiable() = RangeNotSatisfiable()
fun rangeNotSatisfiable(message: String) = RangeNotSatisfiable(message)
fun rangeNotSatisfiable(message: String, cause: Throwable) = RangeNotSatisfiable(message, cause)
fun rangeNotSatisfiable(cause: Throwable) = RangeNotSatisfiable(cause)
fun expectationFailed() = ExpectationFailed()
fun expectationFailed(message: String) = ExpectationFailed(message)
fun expectationFailed(message: String, cause: Throwable) = ExpectationFailed(message, cause)
fun expectationFailed(cause: Throwable) = ExpectationFailed(cause)
fun teapot() = ImATeapot()
fun teapot(message: String) = ImATeapot(message)
fun teapot(message: String, cause: Throwable) = ImATeapot(message, cause)
fun teapot(cause: Throwable) = ImATeapot(cause)
fun preconditionRequired() = PreconditionRequired()
fun preconditionRequired(message: String) = PreconditionRequired(message)
fun preconditionRequired(message: String, cause: Throwable) = PreconditionRequired(message, cause)
fun preconditionRequired(cause: Throwable) = PreconditionRequired(cause)
fun tooManyRequests() = TooManyRequests()
fun tooManyRequests(message: String) = TooManyRequests(message)
fun tooManyRequests(message: String, cause: Throwable) = TooManyRequests(message, cause)
fun tooManyRequests(cause: Throwable) = TooManyRequests(cause)
fun unavailableForLegalReasons() = UnavailableForLegalReasons()
fun unavailableForLegalReasons(message: String) = UnavailableForLegalReasons(message)
fun unavailableForLegalReasons(message: String, cause: Throwable) =
    UnavailableForLegalReasons(message, cause)

fun unavailableForLegalReasons(cause: Throwable) = UnavailableForLegalReasons(cause)
fun badGateway() = BadGateway()
fun badGateway(message: String) = BadGateway(message)
fun badGateway(message: String, cause: Throwable) = BadGateway(message, cause)
fun badGateway(cause: Throwable) = BadGateway(cause)
fun serviceUnavailable() = ServiceUnavailable()
fun serviceUnavailable(message: String) = ServiceUnavailable(message)
fun serviceUnavailable(message: String, cause: Throwable) = ServiceUnavailable(message, cause)
fun serviceUnavailable(cause: Throwable) = ServiceUnavailable(cause)
fun gatewayTimeout() = GatewayTimeout()
fun gatewayTimeout(message: String) = GatewayTimeout(message)
fun gatewayTimeout(message: String, cause: Throwable) = GatewayTimeout(message, cause)
fun gatewayTimeout(cause: Throwable) = GatewayTimeout(cause)
fun variantAlsoNegotiates() = VariantAlsoNegotiates()
fun variantAlsoNegotiates(message: String) = VariantAlsoNegotiates(message)
fun variantAlsoNegotiates(message: String, cause: Throwable) = VariantAlsoNegotiates(message, cause)
fun variantAlsoNegotiates(cause: Throwable) = VariantAlsoNegotiates(cause)
fun insufficientStorage() = InsufficientStorage()
fun insufficientStorage(message: String) = InsufficientStorage(message)
fun insufficientStorage(message: String, cause: Throwable) = InsufficientStorage(message, cause)
fun insufficientStorage(cause: Throwable) = InsufficientStorage(cause)
fun loopDetected() = LoopDetected()
fun loopDetected(message: String) = LoopDetected(message)
fun loopDetected(message: String, cause: Throwable) = LoopDetected(message, cause)
fun loopDetected(cause: Throwable) = LoopDetected(cause)

enum class Redirect {
    MOVED_PERMANENTLY {
        override fun asException(targetUrl: String): HttpRedirectException {
            return HttpRedirectException.MovedPermanently(targetUrl)
        }
    },

    FOUND {
        override fun asException(targetUrl: String): HttpRedirectException {
            return HttpRedirectException.Found(targetUrl)
        }
    },

    SEE_OTHER {
        override fun asException(targetUrl: String): HttpRedirectException {
            return HttpRedirectException.SeeOther(targetUrl)
        }
    },

    TEMPORARY_REDIRECT {
        override fun asException(targetUrl: String): HttpRedirectException {
            return HttpRedirectException.Temporary(targetUrl)
        }
    },

    PERMANENT_REDIRECT {
        override fun asException(targetUrl: String): HttpRedirectException {
            return HttpRedirectException.Permanent(targetUrl)
        }
    };

    internal abstract fun asException(targetUrl: String): HttpRedirectException
}

fun redirect(kind: Redirect, targetUrl: String) = kind.asException(targetUrl)

fun redirect(
    targetUrl: String,
    permanent: Boolean = false,
    preserveMethod: Boolean = false,
    seeOther: Boolean = true
) =
    redirect(
        when (preserveMethod) {
            true -> when (permanent) {
                true -> Redirect.PERMANENT_REDIRECT
                false -> Redirect.TEMPORARY_REDIRECT
            }

            false -> when (permanent) {
                true -> Redirect.MOVED_PERMANENTLY
                false -> when (seeOther) {
                    true -> Redirect.SEE_OTHER
                    false -> Redirect.FOUND
                }
            }
        }, targetUrl
    )
