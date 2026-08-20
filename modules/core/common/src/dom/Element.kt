@file:GenerateServerStubs

package net.derfruhling.serenity.dom

import net.derfruhling.serenity.annotations.GenerateServerStubs
import net.derfruhling.serenity.tree.platform.ElementNode

expect open class Element : EventTarget

expect open class DomValidityState {
    val badInput: Boolean
    val customError: Boolean
    val patternMismatch: Boolean
    val rangeOverflow: Boolean
    val stepMismatch: Boolean
    val tooLong: Boolean
    val tooShort: Boolean
    val typeMismatch: Boolean
    val valid: Boolean
    val valueMissing: Boolean
}

expect interface DomValidationTarget {
    val validationMessage: String
    val validity: DomValidityState
    val willValidate: Boolean

    fun checkValidity(): Boolean
    fun reportValidity(): Boolean
}

expect open class HTMLSelectElement : Element, DomValidationTarget {
    var value: String

    override val validationMessage: String
    override val validity: DomValidityState
    override val willValidate: Boolean
    override fun checkValidity(): Boolean
    override fun reportValidity(): Boolean
    fun setCustomValidity(error: String)
}

expect val Element.node: ElementNode
