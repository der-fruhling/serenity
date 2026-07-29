package net.derfruhling.serenity.annotations

import kotlinx.datetime.Month

@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class WidelyAvailable(val since: Since = Since(year = 2015, month = Month.JULY))
