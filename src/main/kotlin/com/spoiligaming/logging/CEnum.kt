package com.spoiligaming.logging

/**
 * `CEnum` is an enumeration representing various color codes.
 *
 * Each enum constant corresponds to a specific color code used for console output. These color
 * codes are defined by the ANSI Escape sequences, which are used to control the formatting, color,
 * and other output options on the terminal.
 *
 * The enum constants are:
 * - `RESET`: Resets the color. It is used to bring the console color back to the default after it
 *   has been changed.
 * - `GREEN`: Represents the color green.
 * - `BRIGHT_PINK`: Represents a bright pink color, typically used for highlighting or emphasis.
 * - `BRIGHT_PURPLE`: Represents a bright purple color, often used for highlighting or emphasis.
 * - `ERROR_RED`: Represents the color red. This is typically used to display error messages.
 * - `WHITE`: Represents the color white.
 * - `YELLOW`: Represents the color yellow.
 * - `ORANGE`: Represents the color orange.
 * - `BLUE`: Represents the color blue.
 * - `CYAN`: Represents the color cyan.
 * - `RED`: Represents the color red.
 * - `BOLD`: Represents bold text.
 * - `UNDERLINE`: Represents underlined text.
 *
 * Each enum constant holds an escape code, which is a `String` that contains the ANSI escape
 * sequence for the corresponding color.
 *
 * The `toString()` method is overridden to return the escape code when an enum constant is printed.
 */
enum class CEnum(private val escapeCode: String) {
    RESET("\u001B[0m"),
    GREEN("\u001B[32m"),
    HACKY_GREEN("\u001B[32;1m"),
    BRIGHT_PINK("\u001B[95m"),
    BRIGHT_PURPLE("\u001B[35m"),
    ERROR_RED("\u001B[31m"),
    WHITE("\u001B[97m"),
    YELLOW("\u001B[33m"),
    ORANGE("\u001B[38;5;208m"),
    BLUE("\u001B[34m"),
    CYAN("\u001B[36m"),
    RED("\u001B[91m"),
    BOLD("\u001B[1m"),
    UNDERLINE("\u001B[4m"),
    ;

    override fun toString(): String = escapeCode
}
