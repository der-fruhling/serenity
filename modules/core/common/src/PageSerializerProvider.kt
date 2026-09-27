package net.derfruhling.serenity

import kotlinx.serialization.KSerializer
import net.derfruhling.serenity.annotations.UsedByGeneratedCode

@SubclassOptInRequired(UsedByGeneratedCode::class)
interface PageSerializerProvider<T : PageHolder<T>> {
    fun serializer(): KSerializer<T>
}
