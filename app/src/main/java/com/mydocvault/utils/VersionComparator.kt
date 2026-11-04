package com.mydocvault.utils

/** SemVer 2.0 precedence. Length comparison also handles components larger than a Long. */
object VersionComparator {
    private val version = Regex("""[vV]?(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?(?:\+[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*)?""")

    fun isNewerVersion(latest: String, current: String): Boolean {
        val a = parse(latest) ?: return false
        val b = parse(current) ?: return false
        for (index in 1..3) {
            val order = compareNumbers(a[index], b[index])
            if (order != 0) return order > 0
        }
        if (a[4].isEmpty() || b[4].isEmpty()) return a[4].isEmpty() && b[4].isNotEmpty()
        val left = a[4].split('.')
        val right = b[4].split('.')
        for ((x, y) in left.zip(right)) {
            val xNumber = x.all(Char::isDigit)
            val yNumber = y.all(Char::isDigit)
            val order = when {
                xNumber && yNumber -> compareNumbers(x, y)
                xNumber -> -1
                yNumber -> 1
                else -> x.compareTo(y)
            }
            if (order != 0) return order > 0
        }
        return left.size > right.size
    }

    private fun compareNumbers(a: String, b: String): Int =
        if (a.length == b.length) a.compareTo(b) else a.length.compareTo(b.length)

    private fun parse(value: String): List<String>? {
        val groups = version.matchEntire(value)?.groupValues ?: return null
        if (groups[4].split('.').any { it.length > 1 && it.startsWith('0') && it.all(Char::isDigit) }) return null
        return groups
    }
}
