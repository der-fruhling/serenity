package net.derfruhling.serenity.localization

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import net.derfruhling.serenity.manifest.ResourceResolver

private val languageCache = mutableMapOf<LanguageTag, LocalizationResource>()

internal actual tailrec fun <T : Any> LanguageTag.makeLessSpecific(fn: (LanguageTag) -> T?): T? {
    fn(this)?.let { return it }

    val tag = if (scriptString != null && areaString != null) {
        LanguageTag("${languageString}-${areaString}")
    } else if (areaString != null || scriptString != null) {
        LanguageTag(languageString)
    } else return null

    return tag.makeLessSpecific(fn)
}

private val logger = KotlinLogging.logger("net.derfruhling.serenity.localization.FetcherKt")
private val mutex = Mutex()

@OptIn(ExperimentalSerializationApi::class)
actual suspend fun AvailableLocalizations.fetchLanguage(
    tag: LanguageTag,
    resolver: ResourceResolver
): LocalizationResource = mutex.withLock {
    languageCache[tag]?.let { return it }

    val path: String = tag.makeLessSpecific { languages[it] }
        ?: languages[defaultLanguage]!!

    val actualPath = "_static" + resolver.getTargetUrl(path)
    val bytes = ClassLoader.getSystemClassLoader().getResource(actualPath)!!
        .openStream().use { it.readAllBytes() }
    val cbor = try {
        Cbor.decodeFromByteArray<LocalizationResource>(bytes)
    } catch (e: Exception) {
        logger.warn(e) { "Error while decoding localization resource for tag $tag" }

        if (tag == defaultLanguage) {
            throw IllegalStateException("Default language could not be parsed", e)
        } else {
            return fetchLanguage(defaultLanguage)
        }
    }

    languageCache[tag] = cbor
    cbor
}
