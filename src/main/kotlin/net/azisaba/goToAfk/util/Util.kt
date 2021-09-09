package net.azisaba.goToAfk.util

object Util {
    fun String.toRegexOr(regex: Regex): Regex {
        return try {
            this.toRegex()
        } catch (e: Exception) {
            regex
        }
    }
}
