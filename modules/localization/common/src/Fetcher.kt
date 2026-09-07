package net.derfruhling.serenity.localization

import net.derfruhling.serenity.manifest.ResourceResolver

internal expect fun <T : Any> LanguageTag.makeLessSpecific(fn: (LanguageTag) -> T?): T?

expect suspend fun AvailableLocalizations.fetchLanguage(
    tag: LanguageTag,
    resolver: ResourceResolver = ResourceResolver.Default
): LocalizationResource
