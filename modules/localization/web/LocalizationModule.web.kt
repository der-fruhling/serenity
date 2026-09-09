package net.derfruhling.serenity.localization

import js.array.toList
import js.string.JsStrings.toKotlinString
import net.derfruhling.serenity.manifest.ResourceResolver
import net.derfruhling.serenity.modularity.ProvideContext
import web.navigator.navigator

private data class Option(val tag: LanguageTag, val quality: Float)

@OptIn(ExperimentalWasmJsInterop::class)
internal actual suspend fun ProvideContext.actualProvide() {
    val manifest = getManifest()
    val avail = manifest[AvailableLocalizations::class] ?: return
    val resolver = manifest.findOf<ResourceResolver>() ?: ResourceResolver.Default

    val lang = avail.fetchLanguage(avail.defaultLanguage, resolver)
    use(defaultLocalizationResource provides lang)

    navigator.languages.toList().map { it.toKotlinString() }.takeIf { it.isNotEmpty() }?.let {
        val options = mutableListOf<Option>()

        it.forEach { langTagStr ->
            val split = langTagStr.split(';')
            val langTag = LanguageTag(split[0].trim())
            val (qual, value) = split.elementAtOrNull(1)?.split('=') ?: listOf("q", "1.0")
            options.add(Option(langTag, if (qual == "q") value.toFloat() else 1.0f))
        }

        options.sortByDescending { o -> o.quality }

        for ((langTag) in options) {
            var actualTag: LanguageTag = langTag
            langTag.makeLessSpecific { tag -> actualTag = tag; avail.languages[tag] }
                ?: continue

            val lang = avail.fetchLanguage(actualTag, resolver)
            use(language provides actualTag)
            use(loadedLocalizationResource provides lang)
            break
        }
    }
}