package com.mydocvault.ui

import android.graphics.Rect
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.activity.BackEventCompat
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull
import androidx.core.view.allViews
import androidx.test.core.app.ActivityScenario
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.mydocvault.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class PredictiveNavigationTest {
    private val first = "File it for later"
    private val second = "Open to the page"

    // Sample Compose's current layout on the UI thread. Accessibility snapshots can retain
    // an intermediate frame after a graphics-layer transition has finished.
    private fun ActivityScenario<MainActivity>.layout(): Map<String, ComposeRect> {
        var result = emptyMap<String, ComposeRect>()
        onActivity { activity ->
            result = buildMap {
                activity.window.decorView.allViews.filterIsInstance<ViewRootForTest>().forEach { root ->
                    root.semanticsOwner.getAllSemanticsNodes(mergingEnabled = false).forEach { node ->
                        node.config.getOrNull(SemanticsProperties.Text)?.forEach { value ->
                            put(value.text, node.boundsInWindow)
                        }
                    }
                }
            }
        }
        return result
    }

    private fun ActivityScenario<MainActivity>.awaitLayout(
        description: String, matches: (Map<String, ComposeRect>) -> Boolean
    ): Map<String, ComposeRect> {
        val deadline = SystemClock.uptimeMillis() + 20_000
        while (true) {
            val frame = layout()
            if (matches(frame)) return frame
            assertTrue("$description. Last frame: $frame", SystemClock.uptimeMillis() < deadline)
            SystemClock.sleep(35)
        }
    }

    private fun input(command: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand("input $command")).use {
            it.readBytes()
        }
    }

    private fun ActivityScenario<MainActivity>.openReadingPage(): ComposeRect {
        val button = awaitLayout("Guide is visible") { first in it && "Continue" in it }.getValue("Continue")
        input("tap ${button.center.x.toInt()} ${button.center.y.toInt()}")
        awaitLayout("Reading page is active") { second in it && first !in it }
        // Wait for the native artwork and the forward transition to settle before measuring rest.
        InstrumentationRegistry.getInstrumentation().uiAutomation.waitForIdle(450, 10_000)
        return layout().getValue(second)
    }

    @Test @SdkSuppress(minSdkVersion = 34)
    fun systemEdgeSwipeReturnsToPreviousPage() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
            val mode = resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
            assumeTrue("Requires gesture navigation", mode != 0 && resources.getInteger(mode) == 2)
            scenario.openReadingPage()
            val frame = Rect()
            scenario.onActivity { it.window.decorView.getWindowVisibleDisplayFrame(frame) }
            input("touchscreen swipe 1 ${frame.centerY()} ${frame.width() * 2 / 3} ${frame.centerY()} 450")
            scenario.awaitLayout("The system swipe commits back") { first in it && second !in it }
        }
    }

    @Test fun guideSeeksCancelsAndCommitsBack() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val initial = scenario.openReadingPage()
            fun event(progress: Float) = BackEventCompat(1f, 200f, progress, BackEventCompat.EDGE_LEFT)
            fun startBack() {
                scenario.onActivity { it.onBackPressedDispatcher.dispatchOnBackStarted(event(0f)) }
                scenario.onActivity { it.onBackPressedDispatcher.dispatchOnBackProgressed(event(0.6f)) }
                scenario.awaitLayout("The current page moves during the gesture from $initial") {
                    first in it && (it[second]?.left ?: initial.left) > initial.left + 2f
                }
            }
            startBack()
            scenario.onActivity { it.onBackPressedDispatcher.dispatchOnBackCancelled() }
            scenario.awaitLayout("Cancelling restores $initial") {
                val left = it[second]?.left
                first !in it && left != null && kotlin.math.abs(left - initial.left) < 1.5f
            }
            startBack()
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            scenario.awaitLayout("Committing goes back one page") { first in it && second !in it }
        }
    }
}
