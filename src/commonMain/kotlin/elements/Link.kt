package net.derfruhling.html.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import net.derfruhling.html.Element
import net.derfruhling.html.PageHolder
import net.derfruhling.html.Text
import net.derfruhling.html.annotations.Client
import net.derfruhling.html.attribute.Attributes
import net.derfruhling.html.attribute.ComposableAttribute
import net.derfruhling.html.event.ClickEvent
import net.derfruhling.html.event.On
import net.derfruhling.html.navigate

val linkBase = compositionLocalOf { "" }

object Link : GeneralStructure<Link>(), ElementContext<Link.Attr> {
    object Attr : GeneralStructure.Attr(), AttributeContext {
        val href = ComposableAttribute(Attributes.href)
    }

    override val attributes: Attr
        get() = Attr
}

@Composable
fun Link(to: String, fn: @Composable Link.() -> Unit) {
    Element("a") {
        Link.with {
            if (to.startsWith('/')) {
                val linkBase = linkBase.current
                val actualLink = remember(to, linkBase) { linkBase + to }

                attributes.href { actualLink }
            } else {
                attributes.href { to }
            }

            fn()
        }
    }
}

@Composable
fun Link(to: PageHolder, fn: @Composable Link.() -> Unit) {
    Element("a") {
        Link.with {
            val linkBase = linkBase.current
            val actualLink = remember(to, linkBase) { linkBase + to.path }

            attributes.href { actualLink }

            fn()
        }

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
fun Link(text: String, to: String, fn: @Composable Link.() -> Unit) {
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
fun Link(text: String, to: PageHolder, fn: @Composable Link.() -> Unit) {
    Link(to) {
        Text(text)
        fn()
    }
}
