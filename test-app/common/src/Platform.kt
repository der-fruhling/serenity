@file:PlatformDefinitions

package net.derfruhling.serenity.testapp

import net.derfruhling.serenity.annotations.InitFun
import net.derfruhling.serenity.annotations.PlatformDefinitions
import net.derfruhling.serenity.channel.ChannelModule
import net.derfruhling.serenity.channel.ktor.KtorChannelImpl
import net.derfruhling.serenity.localization.LocalizationModule
import net.derfruhling.serenity.modularity.Modules

expect interface PlatformContext

@InitFun
fun commonMain() {
    Modules.install {
        use(LocalizationModule)
        use(ChannelModule) {
            withImpl(KtorChannelImpl())
        }
    }
}
