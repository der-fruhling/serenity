package net.derfruhling.serenity.gradle.resources

import kotlinx.serialization.json.Json
import net.openhft.hashing.LongHashFunction
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class ResourceVendorService : BuildService<BuildServiceParameters.None> {
    val xxh3 = LongHashFunction.xx3()!!

    fun createJson(pretty: Boolean): Json {
        return Json {
            prettyPrint = pretty
            serializersModule = ManifestEntryRegistry.serializersModule
        }
    }
}
