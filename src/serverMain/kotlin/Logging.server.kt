package net.derfruhling.html

import co.touchlab.kermit.Logger

actual inline fun <reified T> Logger.of() = withTag(T::class.qualifiedName!!)
