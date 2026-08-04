package net.derfruhling.serenity.annotations

interface Parser<T> {
    fun parse(string: String): T
}
