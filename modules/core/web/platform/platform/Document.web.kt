package net.derfruhling.serenity.platform

private val currentDocument by lazy { Document(RealDocument.CURRENT) }

val Document.Companion.CURRENT by ::currentDocument
