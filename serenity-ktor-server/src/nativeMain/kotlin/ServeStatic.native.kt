package net.derfruhling.serenity.ktor.server

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.EntityTagVersion
import io.ktor.http.content.VersionCheckResult
import io.ktor.http.defaultForFilePath
import io.ktor.server.plugins.*
import io.ktor.server.request.header
import io.ktor.server.response.cacheControl
import io.ktor.server.response.etag
import io.ktor.server.response.respond
import io.ktor.server.response.respondSource
import io.ktor.server.routing.*
import io.ktor.util.sha1
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import platform.posix.ENOENT
import platform.posix.errno
import platform.posix.stat
import platform.posix.strerror
import kotlin.io.encoding.Base64
import kotlin.time.Instant
import kotlin.to

val staticPath = runCatching { SystemFileSystem.resolve(Path("_static")) }.getOrNull()

private tailrec fun ensureNoSneakyBusiness(parent: Path, expectedDescendent: Path) {
    val descParent = expectedDescendent.parent
    if ((descParent ?: throw NotFoundException()) == parent) {
        return
    } else {
        ensureNoSneakyBusiness(parent, descParent)
    }
}

private data class FileMetadata(
    val lastModified: Instant,
    val size: Long
) {
    constructor(stat: stat) : this(
        lastModified = Instant.fromEpochSeconds(stat.st_mtim.tv_sec, stat.st_mtim.tv_nsec),
        size = stat.st_size
    )
}

private val cachedResources = mutableMapOf<Path, Pair<FileMetadata, Deferred<Buffer>>>()
private val ioScope = CoroutineScope(Dispatchers.IO.limitedParallelism(8))
private val accessMutex = Mutex()

class StaticResourceException(message: String, cause: Exception) : RuntimeException(message, cause)

@OptIn(ExperimentalForeignApi::class)
private suspend fun loadResource(path: Path): Pair<FileMetadata, Buffer> = memScoped {
    val stat = alloc<stat>()
    if (stat(path.toString(), stat.ptr) == -1) {
        when (val error = errno) {
            ENOENT -> throw NotFoundException()
            else -> throw IOException("stat(): ${strerror(error)}")
        }
    }

    val cur = FileMetadata(stat)
    accessMutex.withLock {
        val existing = cachedResources[path]

        if (existing != null) {
            val (meta, buffer) = existing
            if (meta.lastModified <= cur.lastModified) {
                try {
                    return cur to buffer.await()
                } catch (e: Exception) {
                    cachedResources.remove(path)
                    throw StaticResourceException("While loading: $path (cached)", e)
                }
            }
        }

        val deferred = ioScope.async {
            SystemFileSystem.source(path).use { source ->
                val buffer = Buffer()
                var offset = 0L

                while (offset < cur.size) {
                    val readSize = source.readAtMostTo(buffer, cur.size - offset)
                    if (readSize == -1L) break
                    offset += readSize
                }

                buffer
            }
        }.also {
            cachedResources[path] = cur to it
        }

        try {
            return cur to deferred.await()
        } catch (e: Exception) {
            cachedResources.remove(path)
            throw StaticResourceException("While loading: $path (new)", e)
        }
    }
}

actual fun Route.serveStatic(remotePath: String) {
    val logger = KotlinLogging.logger {}

    if(staticPath != null) {
        logger.info { "Serving static resources from: $staticPath" }
        route(remotePath) {
            get("{path...}") {
                val pathString = call.parameters.getAll("path")!!.toTypedArray()
                val path = SystemFileSystem.resolve(Path(staticPath, *pathString))

                ensureNoSneakyBusiness(staticPath, path)

                val (meta, buffer) = loadResource(path)
                val etag = Base64.UrlSafe.encode(sha1(buffer.peek().readByteArray()))

                call.response.cacheControl(CacheControl.MaxAge(maxAgeSeconds = 1800, visibility = CacheControl.Visibility.Public))
                call.response.etag(etag)

                call.request.header("If-None-Match")?.let {
                    if(EntityTagVersion(etag, weak = false).noneMatch(EntityTagVersion.parse(it)) == VersionCheckResult.NOT_MODIFIED) {
                        return@get call.respond(HttpStatusCode.NotModified)
                    }
                }

                call.respondSource(
                    buffer.peek(),
                    ContentType.defaultForFilePath(pathString.last()),
                    status = HttpStatusCode.OK,
                    contentLength = meta.size
                )
            }
        }
    } else {
        logger.info { "Not serving static resources (directory _static not found)" }
    }
}
