package net.derfruhling.serenity.compiler

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object Names {
    val annotationsPackage = FqName("net.derfruhling.serenity.annotations")
    val serialPackage = FqName("net.derfruhling.serenity.serial")
    val pageExtensionPackage = FqName("net.derfruhling.serenity.modularity.extension")
    val corePackage = FqName("net.derfruhling.serenity")
    val composeRuntimePackage = FqName("androidx.compose.runtime")
    val serializationPackage = FqName("kotlinx.serialization")
    val kotlinPackage = FqName("kotlin")
    val kotlinCollections = FqName("kotlin.collections")
    val kotlinReflect = FqName("kotlin.reflect")

    private fun annotation(name: String): ClassId =
        ClassId(annotationsPackage, Name.identifier(name))
    private fun FqName.classOf(name: String): ClassId =
        ClassId(this, Name.identifier(name))
    private fun FqName.callableOf(name: String): CallableId =
        CallableId(this, Name.identifier(name))

    val clientClass: ClassId = annotation("ClientOnly")
    val serverClass: ClassId = annotation("ServerOnly")
    val stubClass: ClassId = annotation("Stub")
    val notifiedRuntimeClass: ClassId = annotation("NotifiesRuntime")
    val pageClass: ClassId = annotation("Page")

    val pageHolderClass: ClassId = corePackage.classOf("PageHolder")
    val pageFactoryClass: ClassId = corePackage.classOf("PageHolderFactory")
    val pageDetailsClass: ClassId = corePackage.classOf("PageDetails")
    val pageContextClass: ClassId = corePackage.classOf("PageContext")
    val pageContractClass: ClassId = corePackage.classOf("PageContract")
    val pageContractImplClass: ClassId = corePackage.classOf("PageContractBuilder")
    val pageContractFn: CallableId = corePackage.callableOf("pageContract")
    val htmlComposableClass: ClassId = corePackage.classOf("HtmlComposable")
    val pageFactoryFinder: CallableId = corePackage.callableOf("pageFactory")
    val pageOfFinder: CallableId = corePackage.callableOf("of")
    val pageContractImplGetDetails: CallableId = CallableId(pageContractImplClass, Name.identifier("getDetails"))
    val pageContractImplGetExtensions: CallableId = CallableId(pageContractImplClass, Name.identifier("getExtensions"))
    val abstractPageExtensionClass: ClassId = pageExtensionPackage.classOf("AbstractPageExtension")
    val serialRegistryClass: ClassId = serialPackage.classOf("SerialRegistry")
    val parameterParserContextClass: ClassId = serialPackage.classOf("ParameterParserContext")
    val parameterParserClass: ClassId = serialPackage.classOf("ParameterParser")

    val androidxStableMarkerClass: ClassId = composeRuntimePackage.classOf("StableMarker")
    val androidxImmutableClass: ClassId = composeRuntimePackage.classOf("Immutable")
    val androidxComposableClass: ClassId = composeRuntimePackage.classOf("Composable")

    val emptyMap: CallableId = kotlinCollections.callableOf("emptyMap")
    val mapClass: ClassId = kotlinCollections.classOf("Map")
    val typeOf: CallableId = kotlinReflect.callableOf("typeOf")
    val kClassClass: ClassId = kotlinReflect.classOf("KClass")
    val illegalArgumentException: ClassId = kotlinPackage.classOf("IllegalArgumentException")

    val transientClass: ClassId = serializationPackage.classOf("Transient")
    val serializableClass: ClassId = serializationPackage.classOf("Serializable")
    val serialNameClass: ClassId = serializationPackage.classOf("SerialName")
}
