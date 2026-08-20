package net.derfruhling.serenity.event

import kotlinx.datetime.Month
import kotlinx.serialization.Serializable
import net.derfruhling.serenity.annotations.Since
import net.derfruhling.serenity.annotations.WidelyAvailable
import net.derfruhling.serenity.dom.Document

@Serializable
@WidelyAvailable(Since(year = 2021, month = Month.APRIL))
data object VisibilityChangeEvent : EventType<Event<Document>>("visibilitychange"),
                                    BuiltinPlainDocumentEvent {
    override val isSupported: Boolean by lazy { testSupportedDocumentEvent(name) }
}
