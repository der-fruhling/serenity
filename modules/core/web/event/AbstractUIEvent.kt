package net.derfruhling.serenity.event

import net.derfruhling.serenity.dom.EventTarget
import web.uievents.UIEvent as DomUIEvent

abstract class AbstractUIEvent<T : EventTarget>(event: DomUIEvent) : AbstractEvent<T>(event),
                                                                     UIEvent<T> {

}
