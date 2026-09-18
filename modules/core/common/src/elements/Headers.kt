@file:Suppress("NOTHING_TO_INLINE")

package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.attribute
import net.derfruhling.serenity.attribute.HtmlAttributes

@Composable
fun H1(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable () -> Unit
) = Element(update = {
    apply(classList)
    attribute(HtmlAttributes.id, id)
}, "h1") { fn() }

@Composable
fun H2(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable () -> Unit
) = Element(update = {
    apply(classList)
    attribute(HtmlAttributes.id, id)
}, "h2") { fn() }

@Composable
fun H3(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable () -> Unit
) = Element(update = {
    apply(classList)
    attribute(HtmlAttributes.id, id)
}, "h3") { fn() }

@Composable
fun H4(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable () -> Unit
) = Element(update = {
    apply(classList)
    attribute(HtmlAttributes.id, id)
}, "h4") { fn() }

@Composable
fun H5(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable () -> Unit
) = Element(update = {
    apply(classList)
    attribute(HtmlAttributes.id, id)
}, "h5") { fn() }

@Composable
fun H6(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable () -> Unit
) = Element(update = {
    apply(classList)
    attribute(HtmlAttributes.id, id)
}, "h6") { fn() }

@Composable
inline fun H1(text: String) = H1 { Text(text) }

@Composable
inline fun H2(text: String) = H2 { Text(text) }

@Composable
inline fun H3(text: String) = H3 { Text(text) }

@Composable
inline fun H4(text: String) = H4 { Text(text) }

@Composable
inline fun H5(text: String) = H5 { Text(text) }

@Composable
inline fun H6(text: String) = H6 { Text(text) }
