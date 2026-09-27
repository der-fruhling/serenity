package net.derfruhling.serenity

import net.derfruhling.serenity.annotations.PageExtensionImplementationApi
import net.derfruhling.serenity.annotations.UsedByGeneratedCode
import net.derfruhling.serenity.modularity.extension.AbstractPageExtension
import kotlin.reflect.KClass

class MultipleExtensionsException : Exception {
    internal constructor(message: String?) : super(message)
}

@PublishedApi
@UsedByGeneratedCode
internal class PageContractBuilder : PageContract {
    private val _extensions = mutableMapOf<KClass<out AbstractPageExtension<*>>, AbstractPageExtension<*>>()

    override var title: String? = null

    @UsedByGeneratedCode
    @PublishedApi
    internal fun getDetails(): PageDetails = PageDetails(title)

    @UsedByGeneratedCode
    @PublishedApi
    internal fun getExtensions(): Map<KClass<out AbstractPageExtension<*>>, AbstractPageExtension<*>> = _extensions.toMap()

    @PageExtensionImplementationApi
    override fun <T : AbstractPageExtension<*>> extend(kClass: KClass<T>, value: T) {
        if(kClass in _extensions) throw MultipleExtensionsException("Class $kClass already has an extension present")
        _extensions[kClass] = value
    }
}
