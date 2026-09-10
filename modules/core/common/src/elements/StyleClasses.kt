package net.derfruhling.serenity.elements

private typealias Augment = AbstractPrefixedStyleClassContainer

object StyleClasses : AbstractPrefixedStyleClassContainer("s", false) {
    val FlexColumn by lazyClass
    val FlexRow by lazyClass
    val PageLayout by lazyClass
    val PageContent by lazyClass
    val TextInput by lazyClass
    val SearchInput by lazyClass

    sealed class Axis(prefix: String) : Augment(prefix) {
        val start by lazyClass
        val center by lazyClass
        val end by lazyClass
    }

    object CrossAxis : Axis("cross") {
        val spaceEvenly by lazyClass
        val spaceAround by lazyClass
        val spaceBetween by lazyClass
    }

    object MainAxis : Axis("main")
}
