package net.derfruhling.serenity.ktor.server

import platform.posix.stat
import kotlin.time.Instant

actual fun stat.modifiedTime(): Instant {
    return Instant.fromEpochSeconds(st_mtim.tv_sec, st_mtim.tv_nsec)
}