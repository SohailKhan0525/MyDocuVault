package com.mydocvault.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mydocvault.ui.screens.GuideContent
import com.mydocvault.ui.screens.GuidePage
import com.mydocvault.ui.theme.MyDocVaultTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class GuideLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test fun instructionsReflowAndContinueStaysReachable() {
        val viewport = mutableStateOf(DpSize(320.dp, 560.dp))
        compose.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(viewport.value)
                then DeviceConfigurationOverride.FontScale(1.3f)) {
                MyDocVaultTheme(darkTheme = false) {
                    GuideContent(GuidePage.Filing, false, {}, {}, {})
                }
            }
        }
        for ((size, wide) in listOf(
            DpSize(320.dp, 560.dp) to false,
            DpSize(620.dp, 320.dp) to true,
            DpSize(840.dp, 900.dp) to true
        )) {
            compose.runOnIdle { viewport.value = size }
            compose.onNodeWithText("Continue").assertIsDisplayed().assertHasClickAction()
            val plate = compose.onNodeWithTag("guide-plate").getUnclippedBoundsInRoot()
            val title = compose.onNodeWithText("File it for later").getUnclippedBoundsInRoot()
            assertTrue("Layout at $size: plate=$plate, title=$title",
                if (wide) title.left >= plate.right else title.top >= plate.bottom)
        }
    }
}
