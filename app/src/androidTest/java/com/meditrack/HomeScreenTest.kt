package com.meditrack

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun homeScreen_displaysGreeting() {
        composeRule.onNodeWithText("Good morning", substring = true).assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysTodayMedsSection() {
        composeRule.onNodeWithText("Today's medications", ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun bottomNav_navigatesToMedications() {
        composeRule.onNodeWithText("Meds").performClick()
        composeRule.onNodeWithText("Medications").assertIsDisplayed()
    }

    @Test
    fun bottomNav_navigatesToVitals() {
        composeRule.onNodeWithText("Vitals").performClick()
        composeRule.onNodeWithText("Vitals tracker").assertIsDisplayed()
    }

    @Test
    fun bottomNav_navigatesToAppointments() {
        composeRule.onNodeWithText("Appts").performClick()
        composeRule.onNodeWithText("Appointments").assertIsDisplayed()
    }

    @Test
    fun medicationsScreen_showsFabButton() {
        composeRule.onNodeWithText("Meds").performClick()
        composeRule.onNodeWithContentDescription("Add medication").assertIsDisplayed()
    }

    @Test
    fun addMedicationScreen_opensAndValidates() {
        composeRule.onNodeWithText("Meds").performClick()
        composeRule.onNodeWithContentDescription("Add medication").performClick()
        composeRule.onNodeWithText("Add medication").assertIsDisplayed()
        // Try save without name — Save button should still be there
        composeRule.onNodeWithText("Save medication").assertIsDisplayed()
    }
}
