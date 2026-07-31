package net.derfruhling.serenity.gradle.resources

import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.kotlin.dsl.registerIfAbsent
import java.io.File
import java.net.URI
import kotlin.io.encoding.Base64
import kotlin.io.path.toPath

abstract class SourceMapFixerService : BuildService<BuildServiceParameters.None> {
    val mappedExtensions = listOf("css", "js")

    private val sourceMapRegex = Regex("""/\*#\s*sourceMappingURL=([^\s*]+)\s*\*/""")
    private val dataRegex = Regex("""data:application/json;base64,([a-zA-Z0-9-_]+=*)""")

    fun fixSourceMaps(
        sourceRootsIter: Iterable<File>,
        destinationRoot: File,
        outputs: FileCollection
    ) {
        val sourceRoots = sourceRootsIter.map { it.absoluteFile }
        for (possibleMap in outputs) {
            if (!possibleMap.isFile) continue
            if (possibleMap.extension == "map") {
                val sourceMap = Json.decodeFromString<SourceMap>(possibleMap.readText())
                val fixed = fixSourceMap(sourceRoots, destinationRoot, possibleMap, sourceMap)
                possibleMap.writeText(Json.encodeToString(fixed))
            } else if (possibleMap.extension in mappedExtensions) {
                val text = possibleMap.readText()
                val fixedText = text.replace(sourceMapRegex) { match ->
                    val sourceMapUrl = match.groups[1]!!
                    val range =
                        sourceMapUrl.range.let { (it.first - match.range.first)..(it.last - match.range.first) }
                    match.value.replaceRange(
                        range,
                        fixUrl(sourceRoots, destinationRoot, possibleMap, sourceMapUrl.value)
                    )
                }

                possibleMap.writeText(fixedText)
            }
        }
    }

    fun fixUrl(
        sourceRoots: Iterable<File>,
        destinationRoot: File,
        mapFile: File,
        url: String
    ): String {
        return if (url.startsWith("file://")) {
            fixFileUrl(sourceRoots, destinationRoot, mapFile, url)
        } else if (url.startsWith("data:")) {
            val data = dataRegex.matchEntire(url)!!
            val b64 = data.groups[1]!!
            val sourceMap =
                Json.decodeFromString<SourceMap>(Base64.UrlSafe.decode(b64.value).decodeToString())
            val fixed = fixSourceMap(sourceRoots, destinationRoot, mapFile, sourceMap)
            url.replaceRange(b64.range, Base64.encode(Json.encodeToString(fixed).toByteArray()))
        } else {
            url
        }
    }

    fun fixFileUrl(
        sourceRoots: Iterable<File>,
        destinationRoot: File,
        mapFile: File,
        url: String
    ): String {
        require(url.startsWith("file://"))
        val uri = URI.create(url)
        var filePath = uri.toPath().toFile()

        for (root in sourceRoots) {
            if (filePath.startsWith(root)) {
                filePath = destinationRoot.resolve(filePath.relativeTo(root))
                break
            }
        }

        return filePath.toRelativeString(mapFile.parentFile!!)
    }

    fun fixSourceMap(
        sourceRoots: Iterable<File>,
        destinationRoot: File,
        mapFile: File,
        sourceMap: SourceMap
    ): SourceMap {
        val newSources = MutableList(sourceMap.sources.size) { sourceMap.sources[it] }
        for ((i, url) in sourceMap.sources.withIndex()) {
            if (sourceMap.sourcesContent != null && sourceMap.sourcesContent.size > i) {
                continue
            }

            newSources[i] = fixUrl(sourceRoots, destinationRoot, mapFile, url)
        }

        return sourceMap.copy(sources = newSources)
    }

    companion object {
        fun registerIfAbsent(target: Project) {
            target.gradle.sharedServices.registerIfAbsent(
                "sourceMapFixer",
                SourceMapFixerService::class
            )
        }
    }
}
