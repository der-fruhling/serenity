package net.derfruhling.serenity.platform

interface StyleHolder {
    fun removed()
    fun setNotifyChanged(fn: () -> Unit)
    fun makeStyle(): String
}