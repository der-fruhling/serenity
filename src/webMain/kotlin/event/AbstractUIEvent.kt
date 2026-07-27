package net.derfruhling.html.event

import web.uievents.UIEvent as DomUIEvent

abstract class AbstractUIEvent<T: EventTarget>(event: DomUIEvent) : AbstractEvent<T>(event), UIEvent<T> {

}
