package net.derfruhling.serenity.compiler

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.name.CallableId

sealed class PageKey : GeneratedDeclarationKey() {
    data class PageClass(val isSubClass: Boolean) : PageKey()
    data object PageFactoryClass : PageKey()
    data object PageSerializerClass : PageKey()
    data object PageIdProperty : PageKey()
    data object PagePathProperty : PageKey()
    data object PageDetailsProperty : PageKey()
    data object PageExtensionsProperty : PageKey()
    data object PageMainFun : PageKey()
    data object PageFactoryCreateFun : PageKey()
    data object PageInitFun : PageKey()
    data object PageFactoryOfFun : PageKey()
    data object Parameter : PageKey()
    data object PageConstructor : PageKey()
    data object PageSerializerGetter : PageKey()
    data object PageSerializeFun : PageKey()
    data object PageDeserializeFun : PageKey()
    data object PageDescriptorProperty : PageKey()
    data class PageEntrypointFun(val callableId: CallableId) : PageKey()
}
