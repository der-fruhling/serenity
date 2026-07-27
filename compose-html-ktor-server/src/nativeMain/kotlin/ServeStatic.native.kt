package net.derfruhling.html.ktor.server

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.defaultForFilePath
import io.ktor.server.plugins.*
import io.ktor.server.response.respondSource
import io.ktor.server.routing.*
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
import platform.posix.ENOENT
import platform.posix.errno
import platform.posix.stat
import platform.posix.strerror
import kotlin.time.Instant
import kotlin.to

val staticPath = SystemFileSystem.resolve(Path("_static"))

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

                while (true) {
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
    route(remotePath) {
        get("{path...}") {
            val pathString = call.parameters.getAll("path")!!.toTypedArray()
            val path = SystemFileSystem.resolve(Path(staticPath, *pathString))

            ensureNoSneakyBusiness(staticPath, path)

            val (meta, buffer) = loadResource(path)
            call.respondSource(
                buffer,
                ContentType.defaultForFilePath(pathString.last()),
                status = HttpStatusCode.OK,
                contentLength = meta.size
            )
        }
    }
}
