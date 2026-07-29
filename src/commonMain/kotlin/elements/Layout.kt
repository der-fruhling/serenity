package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.tree.platform.ElementNode

enum class CrossAxis(internal val augment: String) {
    START(StyleClasses.CrossAxis.start),
    CENTER(StyleClasses.CrossAxis.center),
    END(StyleClasses.CrossAxis.end),
    SPACE_EVENLY(StyleClasses.CrossAxis.spaceEvenly),
    SPACE_BETWEEN(StyleClasses.CrossAxis.spaceBetween),
    SPACE_AROUND(StyleClasses.CrossAxis.spaceAround);

    companion object : ElementNode.ClassMapKey
}

enum class MainAxis(internal val augment: String) {
    START(StyleClasses.MainAxis.start),
    CENTER(StyleClasses.MainAxis.center),
    END(StyleClasses.MainAxis.end);

    companion object : ElementNode.ClassMapKey
}

@Composable
private inline fun Flex(
    styleClass: String,
    crossAxis: CrossAxis?,
    mainAxis: MainAxis?,
    fn: @Composable () -> Unit
) {
    Element(name = "div", update = {
        set(styleClass) { classes.add(styleClass) }
        set(crossAxis) { classMap[CrossAxis] = crossAxis?.augment }
        set(mainAxis) { classMap[MainAxis] = mainAxis?.augment }
    }) {
        fn()
    }
}

@Composable
fun FlexColumn(
    crossAxis: CrossAxis? = null,
    mainAxis: MainAxis? = null,
    fn: @Composable () -> Unit
) {
    Flex(StyleClasses.FlexColumn, crossAxis, mainAxis, fn)
}

@Composable
fun FlexRow(
    crossAxis: CrossAxis? = null,
    mainAxis: MainAxis? = null,
    fn: @Composable () -> Unit
) {
    Flex(StyleClasses.FlexRow, crossAxis, mainAxis, fn)
}

@Composable
fun PageLayout(
    title: String,
    lang: String = "en",
    head: @Composable HeadContext.() -> Unit = {},
    fn: @Composable () -> Unit
) {
    Page(title, lang, head, updateBody = {
        init { classes.add(StyleClasses.PageLayout) }
    }) {
        fn()
    }
}

@Composable
fun Header(fn: @Composable () -> Unit) {
    Element("header") { fn() }
}

@Composable
fun Header(vararg classes: String, fn: @Composable () -> Unit) {
    Element(name = "header", update = {
        set(classes) { this.classes.addAll(classes) }
    }) { fn() }
}

@Composable
fun Content(fn: @Composable () -> Unit) {
    Element(name = "div", update = {
        init { classes.add(StyleClasses.PageContent) }
    }) {
        fn()
    }
}

@Composable
fun Footer(fn: @Composable () -> Unit) {
    Element("footer") { fn() }
}

@Composable
fun Footer(vararg classes: String, fn: @Composable () -> Unit = {}) {
    Element(name = "footer", update = {
        set(classes) { this.classes.addAll(classes) }
    }) { fn() }
}
