package net.derfruhling.serenity.server.ktor

import platform.posix.stat
import kotlin.time.Instant

actual fun stat.modifiedTime(): Instant {
    return Instant.fromEpochSeconds(st_mtimespec.tv_sec, st_mtimespec.tv_nsec)
}
