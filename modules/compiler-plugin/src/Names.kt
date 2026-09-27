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
    val serializationEncodingPackage = FqName("kotlinx.serialization.encoding")
    val serializationDescriptorsPackage = FqName("kotlinx.serialization.descriptors")
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
    val pageSerializerProviderClass: ClassId = corePackage.classOf("PageSerializerProvider")
    val pageFactoryClass: ClassId = corePackage.classOf("PageHolderFactory")
    val pageDetailsClass: ClassId = corePackage.classOf("PageDetails")
    val pageContextClass: ClassId = corePackage.classOf("PageContext")
    val pageContractImplClass: ClassId = corePackage.classOf("PageContractBuilder")
    val pageContractFn: CallableId = corePackage.callableOf("pageContract")
    val htmlComposableClass: ClassId = corePackage.classOf("HtmlComposable")
    val invokeCommonEntryPoint: CallableId = corePackage.callableOf("invokeCommonEntryPoint")
    val pageContractImplGetDetails: CallableId = CallableId(pageContractImplClass, Name.identifier("getDetails"))
    val pageContractImplGetExtensions: CallableId = CallableId(pageContractImplClass, Name.identifier("getExtensions"))
    val abstractPageExtensionClass: ClassId = pageExtensionPackage.classOf("AbstractPageExtension")
    val serialRegistryClass: ClassId = serialPackage.classOf("SerialRegistry")
    val decodeFromObjectExtension: CallableId = serialPackage.callableOf("decodeFromObject")
    val parameterParserContextClass: ClassId = serialPackage.classOf("ParameterParserContext")
    val parameterParserClass: ClassId = serialPackage.classOf("ParameterParser")

    val androidxStableMarkerClass: ClassId = composeRuntimePackage.classOf("StableMarker")
    val androidxImmutableClass: ClassId = composeRuntimePackage.classOf("Immutable")
    val androidxComposableClass: ClassId = composeRuntimePackage.classOf("Composable")

    val mapClass: ClassId = kotlinCollections.classOf("Map")
    val typeOf: CallableId = kotlinReflect.callableOf("typeOf")
    val kClassClass: ClassId = kotlinReflect.classOf("KClass")
    val illegalArgumentException: ClassId = kotlinPackage.classOf("IllegalArgumentException")

    val kSerializerClass: ClassId = serializationPackage.classOf("KSerializer")
    val serialDescriptorClass: ClassId = serializationDescriptorsPackage.classOf("SerialDescriptor")
    val encoderClass: ClassId = serializationEncodingPackage.classOf("Encoder")
    val decoderClass: ClassId = serializationEncodingPackage.classOf("Decoder")
    val compositeEncoderClass: ClassId = serializationEncodingPackage.classOf("CompositeEncoder")
    val compositeDecoderClass: ClassId = serializationEncodingPackage.classOf("CompositeDecoder")
    val buildClassSerialDescriptor: CallableId = serializationDescriptorsPackage.callableOf("buildClassSerialDescriptor")
    val classSerialDescriptorBuilderClass: ClassId = serializationDescriptorsPackage.classOf("ClassSerialDescriptorBuilder")
    val serializerFun: CallableId = serializationPackage.callableOf("serializer")
}
