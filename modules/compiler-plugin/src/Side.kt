package net.derfruhling.serenity.compiler

import org.jetbrains.kotlin.platform.NativePlatform
import org.jetbrains.kotlin.platform.PotentiallyWebPlatform
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.jvm.JvmPlatform

enum class Side {
    CLIENT,
    SERVER;

    companion object {
        fun of(platform: TargetPlatform?): Side? {
            var side = null as Side?

            for (p in platform ?: return null) {
                when (p) {
                    is JvmPlatform, is NativePlatform -> {
                        if (side == CLIENT) return null
                        side = SERVER
                    }

                    is PotentiallyWebPlatform -> {
                        if (p.isWeb) {
                            if (side == SERVER) return null
                            side = CLIENT
                        } else {
                            if (side == CLIENT) return null
                            side = SERVER
                        }
                    }
                }
            }

            return side
        }
    }
}
