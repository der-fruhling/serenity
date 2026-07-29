package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.PageHolder
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.annotations.Client
import net.derfruhling.serenity.attribute.Attributes
import net.derfruhling.serenity.event.ClickEvent
import net.derfruhling.serenity.event.On
import net.derfruhling.serenity.navigate

val linkBase = compositionLocalOf { "" }

@Composable
fun Link(to: String, fn: @Composable () -> Unit) {
    val linkBase = linkBase.current
    val actualLink = remember(to, linkBase) {
        if(to.startsWith('/')) {
            linkBase + to
        } else {
            to
        }
    }

    Element(name = "a", update = {
        set(actualLink) { attribute(Attributes.href, it) }
    }) {
        fn()
    }
}

@Composable
fun Link(to: PageHolder, fn: @Composable () -> Unit) {
    val linkBase = linkBase.current
    val actualLink = remember(to, linkBase) { linkBase + to.path }

    Element(name = "a", update = {
        set(actualLink) { attribute(Attributes.href, it) }
    }) {
        fn()

        On(ClickEvent) @Client {
            it.preventDefault()
            navigate(to)
        }
    }
}

@Composable
fun Link(text: String, to: String) {
    Link(to) {
        Text(text)
    }
}

@Composable
fun Link(text: String, to: String, fn: @Composable () -> Unit) {
    Link(to) {
        Text(text)
        fn()
    }
}

@Composable
fun Link(text: String, to: PageHolder) {
    Link(to) {
        Text(text)
    }
}

@Composable
fun Link(text: String, to: PageHolder, fn: @Composable () -> Unit) {
    Link(to) {
        Text(text)
        fn()
    }
}
