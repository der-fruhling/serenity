package net.derfruhling.serenity.http

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
        constructor() : super(HttpStatus.BAD_REQUEST)
        constructor(message: String?) : super(HttpStatus.BAD_REQUEST, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.BAD_REQUEST, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.BAD_REQUEST, cause)
    }

    open class Unauthorized : HttpStatusException {
        constructor() : super(HttpStatus.UNAUTHORIZED)
        constructor(message: String?) : super(HttpStatus.UNAUTHORIZED, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.UNAUTHORIZED, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.UNAUTHORIZED, cause)
    }

    open class PaymentRequired : HttpStatusException {
        constructor() : super(HttpStatus.PAYMENT_REQUIRED)
        constructor(message: String?) : super(HttpStatus.PAYMENT_REQUIRED, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.PAYMENT_REQUIRED, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.PAYMENT_REQUIRED, cause)
    }

    open class Forbidden : HttpStatusException {
        constructor() : super(HttpStatus.FORBIDDEN)
        constructor(message: String?) : super(HttpStatus.FORBIDDEN, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.FORBIDDEN, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.FORBIDDEN, cause)
    }

    open class NotFound : HttpStatusException {
        constructor() : super(HttpStatus.NOT_FOUND)
        constructor(message: String?) : super(HttpStatus.NOT_FOUND, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.NOT_FOUND, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.NOT_FOUND, cause)
    }

    open class NotAcceptable : HttpStatusException {
        constructor() : super(HttpStatus.NOT_ACCEPTABLE)
        constructor(message: String?) : super(HttpStatus.NOT_ACCEPTABLE, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.NOT_ACCEPTABLE, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.NOT_ACCEPTABLE, cause)
    }

    open class Conflict : HttpStatusException {
        constructor() : super(HttpStatus.CONFLICT)
        constructor(message: String?) : super(HttpStatus.CONFLICT, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.CONFLICT, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.CONFLICT, cause)
    }

    open class Gone : HttpStatusException {
        constructor() : super(HttpStatus.GONE)
        constructor(message: String?) : super(HttpStatus.GONE, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.GONE, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.GONE, cause)
    }

    open class LengthRequired : HttpStatusException {
        constructor() : super(HttpStatus.LENGTH_REQUIRED)
        constructor(message: String?) : super(HttpStatus.LENGTH_REQUIRED, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.LENGTH_REQUIRED, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.LENGTH_REQUIRED, cause)
    }

    open class PreconditionFailed : HttpStatusException {
        constructor() : super(HttpStatus.PRECONDITION_FAILED)
        constructor(message: String?) : super(HttpStatus.PRECONDITION_FAILED, message)
        constructor(message: String?, cause: Throwable?) : super(
            HttpStatus.PRECONDITION_FAILED,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(HttpStatus.PRECONDITION_FAILED, cause)
    }

    open class ContentTooLarge : HttpStatusException {
        constructor() : super(HttpStatus.CONTENT_TOO_LARGE)
        constructor(message: String?) : super(HttpStatus.CONTENT_TOO_LARGE, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.CONTENT_TOO_LARGE, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.CONTENT_TOO_LARGE, cause)
    }

    open class URITooLong : HttpStatusException {
        constructor() : super(HttpStatus.URI_TOO_LONG)
        constructor(message: String?) : super(HttpStatus.URI_TOO_LONG, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.URI_TOO_LONG, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.URI_TOO_LONG, cause)
    }

    open class UnsupportedMediaType : HttpStatusException {
        constructor() : super(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        constructor(message: String?) : super(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message)
        constructor(message: String?, cause: Throwable?) : super(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(HttpStatus.UNSUPPORTED_MEDIA_TYPE, cause)
    }

    open class RangeNotSatisfiable : HttpStatusException {
        constructor() : super(HttpStatus.RANGE_NOT_SATISFIABLE)
        constructor(message: String?) : super(HttpStatus.RANGE_NOT_SATISFIABLE, message)
        constructor(message: String?, cause: Throwable?) : super(
            HttpStatus.RANGE_NOT_SATISFIABLE,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(HttpStatus.RANGE_NOT_SATISFIABLE, cause)
    }

    open class ExpectationFailed : HttpStatusException {
        constructor() : super(HttpStatus.EXPECTATION_FAILED)
        constructor(message: String?) : super(HttpStatus.EXPECTATION_FAILED, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.EXPECTATION_FAILED, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.EXPECTATION_FAILED, cause)
    }

    open class ImATeapot : HttpStatusException {
        constructor() : super(HttpStatus.IM_A_TEAPOT)
        constructor(message: String?) : super(HttpStatus.IM_A_TEAPOT, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.IM_A_TEAPOT, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.IM_A_TEAPOT, cause)
    }

    open class PreconditionRequired : HttpStatusException {
        constructor() : super(HttpStatus.PRECONDITION_REQUIRED)
        constructor(message: String?) : super(HttpStatus.PRECONDITION_REQUIRED, message)
        constructor(message: String?, cause: Throwable?) : super(
            HttpStatus.PRECONDITION_REQUIRED,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(HttpStatus.PRECONDITION_REQUIRED, cause)
    }

    open class TooManyRequests : HttpStatusException {
        constructor() : super(HttpStatus.TOO_MANY_REQUESTS)
        constructor(message: String?) : super(HttpStatus.TOO_MANY_REQUESTS, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.TOO_MANY_REQUESTS, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.TOO_MANY_REQUESTS, cause)
    }

    open class UnavailableForLegalReasons : HttpStatusException {
        constructor() : super(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS)
        constructor(message: String?) : super(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS, message)
        constructor(message: String?, cause: Throwable?) : super(
            HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS, cause)
    }

    open class BadGateway : HttpStatusException {
        constructor() : super(HttpStatus.BAD_GATEWAY)
        constructor(message: String?) : super(HttpStatus.BAD_GATEWAY, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.BAD_GATEWAY, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.BAD_GATEWAY, cause)
    }

    open class ServiceUnavailable : HttpStatusException {
        constructor() : super(HttpStatus.SERVICE_UNAVAILABLE)
        constructor(message: String?) : super(HttpStatus.SERVICE_UNAVAILABLE, message)
        constructor(message: String?, cause: Throwable?) : super(
            HttpStatus.SERVICE_UNAVAILABLE,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(HttpStatus.SERVICE_UNAVAILABLE, cause)
    }

    open class GatewayTimeout : HttpStatusException {
        constructor() : super(HttpStatus.GATEWAY_TIMEOUT)
        constructor(message: String?) : super(HttpStatus.GATEWAY_TIMEOUT, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.GATEWAY_TIMEOUT, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.GATEWAY_TIMEOUT, cause)
    }

    open class VariantAlsoNegotiates : HttpStatusException {
        constructor() : super(HttpStatus.VARIANT_ALSO_NEGOTIATES)
        constructor(message: String?) : super(HttpStatus.VARIANT_ALSO_NEGOTIATES, message)
        constructor(message: String?, cause: Throwable?) : super(
            HttpStatus.VARIANT_ALSO_NEGOTIATES,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(HttpStatus.VARIANT_ALSO_NEGOTIATES, cause)
    }

    open class InsufficientStorage : HttpStatusException {
        constructor() : super(HttpStatus.INSUFFICIENT_STORAGE)
        constructor(message: String?) : super(HttpStatus.INSUFFICIENT_STORAGE, message)
        constructor(message: String?, cause: Throwable?) : super(
            HttpStatus.INSUFFICIENT_STORAGE,
            message,
            cause
        )

        constructor(cause: Throwable?) : super(HttpStatus.INSUFFICIENT_STORAGE, cause)
    }

    open class LoopDetected : HttpStatusException {
        constructor() : super(HttpStatus.LOOP_DETECTED)
        constructor(message: String?) : super(HttpStatus.LOOP_DETECTED, message)
        constructor(message: String?, cause: Throwable?) : super(HttpStatus.LOOP_DETECTED, message, cause)
        constructor(cause: Throwable?) : super(HttpStatus.LOOP_DETECTED, cause)
    }
}