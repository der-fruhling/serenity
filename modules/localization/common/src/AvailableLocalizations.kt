package net.derfruhling.serenity.localization

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.derfruhling.serenity.manifest.SharedManifestEntry

@Serializable
@SerialName($$"$localizations")
@Immutable
data class AvailableLocalizations(
    val defaultLanguage: LanguageTag,
    val languages: Map<LanguageTag, String>
) : SharedManifestEntry {
    override val provide: Array<ProvidedValue<*>>
        get() = arrayOf(localizations provides this, language providesDefault LanguageTag.en)

    companion object {
        val empty: AvailableLocalizations = AvailableLocalizations(LanguageTag.en, emptyMap())
    }
}

val localizations = staticCompositionLocalOf { AvailableLocalizations.empty }
val language = compositionLocalWithComputedDefaultOf { localizations.currentValue.defaultLanguage }
val defaultLocalizationResource = staticCompositionLocalOf<LocalizationResource> {
    throw UnsupportedOperationException("No localization loaded")
}
val loadedLocalizationResource = staticCompositionLocalOf<LocalizationResource> {
    throw UnsupportedOperationException("No localization loaded")
}
