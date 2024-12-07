package com.spoiligaming.explorer.utils

object MacUtility {
    fun isOpenGLSupportedOnMac(): Boolean =
        System.getProperty("os.version")
            ?.split(".")
            ?.getOrNull(0)
            ?.toIntOrNull()
            ?.let { it < 14 }
            ?: false

    fun isMetalSupportedOnMac(): Boolean =
        System.getProperty("os.version")
            ?.split(".")
            ?.getOrNull(0)
            ?.toIntOrNull()
            ?.let { it >= 11 }
            ?: false
}
