package net.derfruhling.html.attribute

interface AttributeValue {
    val asValue: String

    companion object {
        fun of(value: Any?): String? =
            when(value) {
                Unit -> ""
                is AttributeValue -> value.asValue
                is String -> value
                is Set<*> -> value.filterNotNull().joinToString(" ") { of(it)!! }
                true -> ""
                false, null -> null
                else -> value.toString()
            }
    }
}