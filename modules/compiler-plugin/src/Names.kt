package net.derfruhling.serenity.compiler

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object Names {
    val annotationsPackage = FqName("net.derfruhling.serenity.annotations")
    val composeRuntimePackage = FqName("androidx.compose.runtime")

    private fun annotation(name: String): ClassId =
        ClassId(annotationsPackage, Name.identifier(name))
    private fun FqName.classOf(name: String): ClassId =
        ClassId(this, Name.identifier(name))

    val clientClass: ClassId = annotation("ClientOnly")
    val serverClass: ClassId = annotation("ServerOnly")
    val stubClass: ClassId = annotation("Stub")
    val notifiedRuntimeClass: ClassId = annotation("NotifiesRuntime")

    val androidxStableMarkerClass: ClassId = composeRuntimePackage.classOf("StableMarker")
    val androidxImmutableClass: ClassId = composeRuntimePackage.classOf("Immutable")
}
