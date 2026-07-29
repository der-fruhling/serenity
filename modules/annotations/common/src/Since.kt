package net.derfruhling.serenity.annotations

import kotlinx.datetime.Month

@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class Since(val year: Int, val month: Month)
