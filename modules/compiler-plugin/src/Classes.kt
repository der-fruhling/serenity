package net.derfruhling.serenity.compiler

import org.jetbrains.kotlin.name.ClassId

val clientClass = ClassId.fromString("net/derfruhling/serenity/annotations/ClientOnly")
val serverClass = ClassId.fromString("net/derfruhling/serenity/annotations/ServerOnly")
