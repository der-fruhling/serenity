package net.derfruhling.serenity

abstract class PageContext {
    abstract fun getParameter(name: String): String?
}
