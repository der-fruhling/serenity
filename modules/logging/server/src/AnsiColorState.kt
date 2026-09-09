package net.derfruhling.serenity.logging

data class AnsiColorState(
    val fg: AnsiColorCode? = null,
    val bold: Boolean = false,
    val underline: Boolean = false,
    val bg: AnsiColorCode? = null
) {
    val value: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        buildString {
            append("\u001b[")
            append(buildList {
                if (fg != null) add(fg.fg)
                if (bold) add(BOLD)
                if (underline) add(UNDERLINE)
                if (bg != null) add(bg.bg)
            }.joinToString(";"))
            append('m')
        }
    }

    companion object {
        private const val BOLD = "1"
        private const val UNDERLINE = "4"

        const val RESET = "\u001b[0m"
    }
}