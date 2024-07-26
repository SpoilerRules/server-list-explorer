package com.spoiligaming.logging

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Logger {
    private val dateTimeFormatter by lazy { DateTimeFormatter.ofPattern("HH:mm:ss") }

    fun <V> printError(error: V) = log("ERROR", error, CEnum.ERROR_RED)

    fun <V> printSuccess(message: V) = log("OK", message, CEnum.GREEN)

    fun <V> printWarning(warning: V) = log("WARNING", warning, CEnum.YELLOW)

    private fun <V> log(
        level: String,
        message: V,
        color: CEnum,
    ) = println("${createStatus(color, level)} $message")

    private fun formatDateTime(): String =
        "${CEnum.RESET}[${CEnum.CYAN}${LocalDateTime.now().format(
            dateTimeFormatter,
        )}${CEnum.RESET}]"

    private fun createStatus(
        primaryColor: CEnum,
        status: String,
    ): String = "${CEnum.RESET}${formatDateTime()} [${primaryColor}${status}${CEnum.RESET}]"
}
