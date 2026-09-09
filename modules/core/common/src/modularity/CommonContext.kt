package net.derfruhling.serenity.modularity

import net.derfruhling.serenity.manifest.Manifest

interface CommonContext {
    suspend fun getManifest(): Manifest
}