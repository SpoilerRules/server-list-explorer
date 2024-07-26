package com.spoiligaming.explorer.utils

object TipLoader {
    private var lastTip: String? = null
    private val tips: List<String> by lazy {
        TipLoader::class
            .java
            .classLoader
            .getResourceAsStream("tips-english.txt")
            ?.bufferedReader()
            ?.readText()
            ?.split("^_^") ?: throw IllegalArgumentException("Tips file not found")
    }

    fun getRandomTip(): String {
        var newTip: String

        do {
            newTip = tips.random()
        } while (newTip == lastTip)
        lastTip = newTip

        return newTip
    }
}
