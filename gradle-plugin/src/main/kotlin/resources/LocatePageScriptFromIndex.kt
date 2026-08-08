package net.derfruhling.serenity.gradle.resources

import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class LocatePageScriptFromIndex : LocatePageScript() {
    @get:InputFile
    abstract val indexFile: RegularFileProperty

    @TaskAction
    fun locate() {
        val json = vendorService.get().createJson(prettyJson.get())
        val index =
            json.decodeFromString<ManifestEntry>(indexFile.get().asFile.readText()) as ResourceIndexBuilder
        val js = index.contents.entries.find { (key, _) -> key.startsWith("js/page.") && key.endsWith(".js") }?.value
        val wasm =
            index.contents.entries.find { (key, _) -> key.startsWith("js/wasm/page.") && key.endsWith(".js") }?.value
        val wasmBinary =
            index.contents.entries.find { (key, _) -> key.startsWith("js/wasm/page.") && key.endsWith(".wasm") }?.value

        outputFile.get().asFile.writeText(
            json.encodeToString<ManifestEntry>(ScriptLocation(js, wasm, wasmBinary))
        )

        didWork = js != null || wasm != null
    }
}
