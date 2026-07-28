package net.derfruhling.html.gradle.resources

import kotlinx.serialization.json.Json
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.file.RelativePath
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.Sync
import java.nio.ByteBuffer
import java.nio.file.Files
import kotlin.io.encoding.Base64

abstract class VendorResourcesTask : Sync() {
    @get:OutputFile
    abstract val resourceIndexFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val sourceBaseUrl: Property<String>

    @get:Input
    @get:Optional
    abstract val targetBaseUrl: Property<String>

    @get:Input
    abstract val prettyJson: Property<Boolean>

    @get:Internal
    abstract val resourceIndex: MapProperty<String, String>

    @get:ServiceReference("resourceVendor")
    abstract val vendorService: Property<ResourceVendorService>

    init {
        includeEmptyDirs = false
        eachFile {
            val origPath = path
            if (name.endsWith(".wasm")) {
                path = name
                resourceIndex.put(origPath, path)
            } else {
                val bytes = Files.readAllBytes(file.toPath())
                val hash = vendorService.get().xxh3.hashBytes(bytes)
                val hashName =
                    "${
                        Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
                            .encode(ByteBuffer.allocate(8).also { it.putLong(hash) }.array())
                    }.${file.extension}"
                relativePath = RelativePath(relativePath.isFile, hashName)
                resourceIndex.put(origPath, hashName)
            }
        }

        doLast {
            val json = vendorService.get().createJson(prettyJson.get())

            resourceIndexFile.asFile.get().writeText(
                json.encodeToString<ManifestEntry>(
                    ResourceIndexBuilder(
                        sourceBaseUrl.orNull,
                        targetBaseUrl.orNull,
                        resourceIndex.get()
                    )
                )
            )
        }
    }
}