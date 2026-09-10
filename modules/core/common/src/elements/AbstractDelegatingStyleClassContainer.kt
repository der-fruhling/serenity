package net.derfruhling.serenity.elements

abstract class AbstractDelegatingStyleClassContainer(
    prefix: String,
    base: AbstractPrefixedStyleClassContainer
) : AbstractPrefixedStyleClassContainer(base.prefix + '-' + hyphenate(prefix))
