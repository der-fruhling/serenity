package net.derfruhling.serenity.event

import kotlinx.datetime.Month
import net.derfruhling.serenity.annotations.*

@WidelyAvailable(Since(year = 2020, month = Month.JULY))
interface PointerEvent<T : EventTarget> : MouseEvent<T> {
    @NewWebApi
    @NewlyAvailable(Since(year = 2024, month = Month.DECEMBER))
    val altitudeAngle: Float?

    @NewWebApi
    @NewlyAvailable(Since(year = 2024, month = Month.DECEMBER))
    val azimuthAngle: Float?

    val width: Double
    val height: Double
    val isPrimary: Boolean

    @LimitedAvailability
    @UnsupportedOnSafari
    val persistentDeviceId: Int?

    val pointerId: Int
    val pointerType: PointerType
    val pressure: Float
    val tangentialPressure: Float
    val tiltX: Int
    val tiltY: Int
    val twist: Int

    /**
     * This seems to be "limited availability" because Firefox for Android
     * always returns an empty array.
     */
    @LimitedAvailability
    fun getCoalescedEvents(): List<PointerEvent<T>>

    @NewWebApi
    @NewlyAvailable(Since(year = 2024, Month.DECEMBER))
    fun getPredictedEvents(): List<PointerEvent<T>>
}