package com.mydocvault.utils

import org.junit.Assert.*
import org.junit.Test

class VersionComparatorTest {
    @Test fun precedence() {
        val versions = listOf("1.9.9", "1.10.0-alpha", "1.10.0-alpha.1", "1.10.0-alpha.beta",
            "1.10.0-beta.2", "1.10.0-beta.11", "1.10.0-rc.1", "v1.10.0", "2.0.0")
        for ((before, after) in versions.zipWithNext()) {
            assertTrue(VersionComparator.isNewerVersion(after, before))
            assertFalse(VersionComparator.isNewerVersion(before, after))
        }
        assertFalse(VersionComparator.isNewerVersion("v1.14.0+build.2", "1.14.0+build.1"))
        for (bad in listOf("nightly", "v1.2", "v1.02.0", "1.14.0-01")) {
            assertFalse(VersionComparator.isNewerVersion(bad, "1.0.0"))
        }
    }
}
