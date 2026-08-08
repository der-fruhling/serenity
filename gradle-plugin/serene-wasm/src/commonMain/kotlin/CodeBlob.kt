package net.derfruhling.serene.wasm

import kotlinx.io.bytestring.ByteString
import kotlin.jvm.JvmInline

@JvmInline
value class CodeBlob(val byteString: ByteString)
