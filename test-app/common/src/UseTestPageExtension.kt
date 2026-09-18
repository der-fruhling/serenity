package net.derfruhling.serenity.testapp

import net.derfruhling.serenity.annotations.ExtendsWith

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@ExtendsWith(TestPageExtension::class)
annotation class UseTestPageExtension()
