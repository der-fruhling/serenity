package net.derfruhling.serenity.gradle.resources

import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class LocatePageScriptFromDirectory : LocatePageScript() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDir: DirectoryProperty

    init {
        outputs.doNotCacheIf("miaw") { true }
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun locate() {
        val json = vendorService.get().createJson(prettyJson.get())
        val js = sourceDir.get().dir("js")
        val jsFile = js.asFile.listFiles()?.find { it.isFile && it.parentFile == js.asFile && it.name.startsWith("page.") && it.extension == "js" }
        val wasm = js.dir("wasm")
        val wasmFile = wasm.asFile.listFiles()?.find { it.isFile && it.parentFile == wasm.asFile && it.name.startsWith("page.") && it.extension == "js" }
        val wasmBinary = wasm.asFile.listFiles()?.find { it.isFile && it.parentFile == wasm.asFile && it.extension == "wasm" }

        outputFile.get().asFile.writeText(
            json.encodeToString<ManifestEntry>(ScriptLocation(
                jsFile?.toRelativeString(sourceDir.get().asFile),
                wasmFile?.toRelativeString(sourceDir.get().asFile),
                wasmBinary?.toRelativeString(sourceDir.get().asFile),
            ))
        )

        didWork = jsFile != null || wasmFile != null
    }
}
