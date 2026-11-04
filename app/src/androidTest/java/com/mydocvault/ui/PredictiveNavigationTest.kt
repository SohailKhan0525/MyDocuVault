package com.mydocvault.ui

import android.os.ParcelFileDescriptor
import androidx.activity.BackEventCompat
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.mydocvault.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PredictiveNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test @SdkSuppress(minSdkVersion = 34)
    fun systemEdgeSwipeReturnsToPreviousPage() {
        val resources = compose.activity.resources
        val mode = resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
        org.junit.Assume.assumeTrue("Requires gesture navigation", mode != 0 && resources.getInteger(mode) == 2)
        compose.waitUntil(20_000) {
            compose.onAllNodesWithText("File it for later").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Continue").performClick()
        compose.onNodeWithText("Open to the page").assertIsDisplayed()
        val metrics = resources.displayMetrics
        val y = metrics.heightPixels / 2
        val input = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
            "input swipe 1 $y ${metrics.widthPixels * 2 / 3} $y 450"
        )
        ParcelFileDescriptor.AutoCloseInputStream(input).use { it.readBytes() }
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Open to the page").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithText("File it for later").assertIsDisplayed()
    }

    @Test fun guideSeeksCancelsAndCommitsBack() {
        val filing = "File it for later"
        val reading = "Open to the page"
        compose.waitUntil(20_000) { compose.onAllNodesWithText(filing).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Continue").performClick()
        val current = compose.onNodeWithText(reading)
        current.assertIsDisplayed()
        val initial = current.fetchSemanticsNode().boundsInRoot
        val dispatcher = compose.activity.onBackPressedDispatcher
        fun seek(progress: Float) = BackEventCompat(1f, 200f, progress, BackEventCompat.EDGE_LEFT)
        fun startBack() {
            compose.runOnIdle { dispatcher.dispatchOnBackStarted(seek(0f)) }
            compose.runOnIdle { dispatcher.dispatchOnBackProgressed(seek(0.6f)) }
            compose.waitUntil(5_000) { compose.onAllNodesWithText(filing).fetchSemanticsNodes().isNotEmpty() }
        }
        startBack()
        compose.waitUntil(5_000) { current.fetchSemanticsNode().boundsInRoot.left > initial.left + 1f }
        compose.runOnIdle { dispatcher.dispatchOnBackCancelled() }
        compose.waitUntil(5_000) { compose.onAllNodesWithText(filing).fetchSemanticsNodes().isEmpty() }
        current.assertIsDisplayed()
        assertTrue(kotlin.math.abs(current.fetchSemanticsNode().boundsInRoot.left - initial.left) < 1f)
        startBack()
        compose.runOnIdle { dispatcher.onBackPressed() }
        compose.onNodeWithText(filing).assertIsDisplayed()
        compose.onNodeWithText(reading).assertDoesNotExist()
    }
}
