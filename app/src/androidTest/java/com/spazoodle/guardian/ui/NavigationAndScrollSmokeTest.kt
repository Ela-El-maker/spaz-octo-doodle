package com.spazoodle.guardian.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spazoodle.guardian.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationAndScrollSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun coreScreens_navigateAndScroll_withoutCrashing() {
        composeRule.onNodeWithText("Guardian Alarms").assertIsDisplayed()
        composeRule.onNodeWithText("Guardian Alarms").performTouchInput {
            swipeUp()
            swipeDown()
        }

        composeRule.onNodeWithText("Reliability Dashboard").performClick()
        composeRule.onNodeWithText("Health Score:", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Health Score:", substring = true).performTouchInput {
            swipeUp()
            swipeDown()
        }
        composeRule.onNodeWithText("Back").performClick()

        composeRule.onNodeWithText("History & Proof").performClick()
        composeRule.onNodeWithText("Copy Diagnostics").assertIsDisplayed()
        composeRule.onNodeWithText("Copy Diagnostics").performTouchInput {
            swipeLeft()
            swipeRight()
        }
        composeRule.onNodeWithText("Back").performClick()

    }
}
