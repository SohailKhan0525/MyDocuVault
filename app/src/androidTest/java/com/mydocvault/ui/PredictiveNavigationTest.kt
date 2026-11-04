package com.mydocvault.ui

import android.graphics.Rect
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.activity.BackEventCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.mydocvault.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class PredictiveNavigationTest {
    private val automation get() = InstrumentationRegistry.getInstrumentation().uiAutomation
    private val first = "File it for later"
    private val second = "Open to the page"

    // Use the platform clock and accessibility tree so Navigation runs on Android's main looper.
    private fun bounds(label: String): Rect? {
        val root = automation.rootInActiveWindow ?: return null
        val node = root.findAccessibilityNodeInfosByText(label)
            .firstOrNull { it.text?.toString() == label && it.isVisibleToUser } ?: return null
        return Rect().also { node.getBoundsInScreen(it) }
    }

    private fun await(description: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 20_000
        while (!condition()) {
            assertTrue(description, SystemClock.uptimeMillis() < deadline)
            SystemClock.sleep(40)
        }
    }

    private fun input(command: String) {
        ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand("input $command")).use {
            it.readBytes()
        }
    }

    private fun openReadingPage() {
        await("Guide is visible") { bounds(first) != null && bounds("Continue") != null }
        val button = requireNotNull(bounds("Continue"))
        input("tap ${button.centerX()} ${button.centerY()}")
        await("Reading page is active") { bounds(second) != null && bounds(first) == null }
    }

    @Test @SdkSuppress(minSdkVersion = 34)
    fun systemEdgeSwipeReturnsToPreviousPage() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
            val mode = resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
            assumeTrue("Requires gesture navigation", mode != 0 && resources.getInteger(mode) == 2)
            openReadingPage()
            val frame = Rect()
            scenario.onActivity { it.window.decorView.getWindowVisibleDisplayFrame(frame) }
            input("swipe 1 ${frame.centerY()} ${frame.width() * 2 / 3} ${frame.centerY()} 450")
            await("The system swipe commits back") { bounds(first) != null && bounds(second) == null }
        }
    }

    @Test fun guideSeeksCancelsAndCommitsBack() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            openReadingPage()
            val initial = requireNotNull(bounds(second))
            fun seek(progress: Float) = BackEventCompat(1f, 200f, progress, BackEventCompat.EDGE_LEFT)
            fun startBack() {
                scenario.onActivity { it.onBackPressedDispatcher.dispatchOnBackStarted(seek(0f)) }
                scenario.onActivity { it.onBackPressedDispatcher.dispatchOnBackProgressed(seek(0.6f)) }
                await("The current page moves while the gesture is held") {
                    (bounds(second)?.left ?: initial.left) > initial.left + 2
                }
            }
            startBack()
            scenario.onActivity { it.onBackPressedDispatcher.dispatchOnBackCancelled() }
            await("Cancelling restores the original page position") {
                bounds(second)?.left == initial.left && bounds(first) == null
            }
            startBack()
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            await("Committing goes back one page") { bounds(first) != null && bounds(second) == null }
        }
    }
}
