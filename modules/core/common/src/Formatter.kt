package net.derfruhling.serenity

fun interface Formatter {
    fun write(string: String)

    val deepIntrospect: Boolean get() = false

    fun begin(type: Begin) {}
    fun end() {}
    fun open() = write(" ")
    fun close() = write(" ")

    fun open(atEnd: Formatter.() -> Unit = {}, fn: Formatter.() -> Unit) {
        open()
        try {
            fn()
        } finally {
            close()
            atEnd()
        }
    }

    fun enter(begin: Begin, name: String, fn: Formatter.() -> Unit) {
        enter(begin) {
            write("$name {")
            open(atEnd = { write("}") }) {
                fn()
            }
        }
    }

    fun enter(begin: Begin, fn: Formatter.() -> Unit) {
        begin(begin)
        try {
            fn()
        } finally {
            end()
        }
    }

    fun block(name: String, fn: Formatter.() -> Unit) {
        enter(Begin.BLOCK, name, fn)
    }

    fun value(name: String, fn: Formatter.() -> Any) {
        begin(Begin.VALUE)
        write("$name =")
        open()
        try {
            when (val returnValue = fn()) {
                is Unit -> {}
                else -> write(returnValue.toString())
            }
        } finally {
            try {
                close()
            } finally {
                end()
            }
        }
    }

    enum class Begin {
        BLOCK,
        DOCUMENT,
        ELEMENT,
        ATTRIBUTE,
        TEXT,
        VALUE,
        DEBUG
    }

    open class StringFormatter(private val stringBuilder: StringBuilder = StringBuilder()) :
        Formatter {
        override fun write(string: String) {
            stringBuilder.append(string)
        }

        fun done() = stringBuilder.toString()
    }

    open class StringFormatterDebug(stringBuilder: StringBuilder = StringBuilder()) :
        StringFormatter(stringBuilder) {
        override val deepIntrospect: Boolean
            get() = true
    }

    companion object {
        inline fun formatString(fn: Formatter.() -> Unit): String {
            return StringFormatter().apply(fn).done()
        }

        inline fun formatStringDebug(fn: Formatter.() -> Unit): String {
            return StringFormatterDebug().apply(fn).done()
        }
    }
}
