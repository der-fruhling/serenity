package net.derfruhling.serenity.event

import kotlinx.serialization.Serializable

@Serializable
data object PageHideEvent : EventType<PageTransitionEvent>("visibilitychange"),
                            BuiltinPageTransitionEvent
