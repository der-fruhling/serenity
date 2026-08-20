package net.derfruhling.serenity.tree.platform

interface StyleHolder {
    fun removed()
    fun setNotifyChanged(fn: () -> Unit)
    fun makeStyle(): String
}