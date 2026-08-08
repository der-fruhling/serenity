package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import com.skydoves.compose.stability.runtime.TraceRecomposition
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
    crossinline fn: @Composable () -> Unit
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
fun Header(fn: @Composable () -> Unit) {
    Element("header") { fn() }
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
