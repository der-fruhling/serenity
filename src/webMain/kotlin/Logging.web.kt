package net.derfruhling.html

import co.touchlab.kermit.Logger

actual inline fun <reified T> Logger.of(): Logger = withTag(T::class.simpleName!!)
