package net.derfruhling.serenity.http

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
