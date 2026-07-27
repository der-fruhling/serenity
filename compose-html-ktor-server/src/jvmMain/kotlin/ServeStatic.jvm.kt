package net.derfruhling.html.ktor.server

import io.ktor.http.CacheControl
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.EntityTagVersion
import io.ktor.http.content.VersionCheckResult
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.path
import java.io.File
import java.net.URL
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

private val messageDigest = ThreadLocal.withInitial { MessageDigest.getInstance("SHA-1") }!!

private class ETagCache<R> {
    private fun create(it: R): EntityTagVersion = when(it) {
        is File -> {
            EntityTagVersion(Base64.UrlSafe.encode(messageDigest.get().digest(it.readBytes())), weak = false)
        }

        is URL -> {
            EntityTagVersion(Base64.UrlSafe.encode(messageDigest.get().digest(it.readBytes())), weak = false)
        }

        else -> error("What is $it?")
    }

    private val cachedValues = mutableMapOf<R, Pair<Instant, EntityTagVersion>>()

    fun forItem(it: R): EntityTagVersion {
        synchronized(cachedValues) {
            return cachedValues.compute(it) { key, value ->
                if(value != null && value.first > Clock.System.now()) {
                    value
                } else {
                    (Clock.System.now() + 15.minutes) to create(it)
                }
            }!!.second
        }
    }
}

actual fun Route.serveStatic(remotePath: String) {
    val etags = ETagCache<URL>()
    application.attributes[staticFilePath] = staticResources(remotePath, "_static", null) {
        etag { etags.forItem(it as URL) }

        cacheControl { listOf(CacheControl.MaxAge(
            maxAgeSeconds = 1800,
            visibility = CacheControl.Visibility.Public
        )) }

        enableAutoHeadResponse()
    }.path
}
