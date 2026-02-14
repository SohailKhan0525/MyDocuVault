package com.mydocvault.utils

object VersionComparator {
    fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.trimStart('v', 'V').split(".")
        val currentParts = current.trimStart('v', 'V').split(".")
        val max = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until max) {
            val l = latestParts.getOrNull(i)?.toIntOrNull() ?: 0
            val c = currentParts.getOrNull(i)?.toIntOrNull() ?: 0
            if (l != c) return l > c
        }
        return false
    }
}
