package net.derfruhling.serenity.logging

enum class AnsiColorCode(val fg: String, val bg: String) {
    BLACK("30", "40"),
    RED("31", "41"),
    GREEN("32", "42"),
    YELLOW("33", "43"),
    BLUE("34", "44"),
    PURPLE("35", "45"),
    CYAN("36", "46"),
    LIGHT_GRAY("37", "47"),
    GRAY("90", "100"),
    BRIGHT_RED("91", "101"),
    BRIGHT_GREEN("92", "102"),
    BRIGHT_YELLOW("93", "103"),
    BRIGHT_BLUE("94", "104"),
    BRIGHT_PURPLE("95", "105"),
    BRIGHT_CYAN("96", "106"),
    BRIGHT_WHITE("97", "107"),
}
