package com.ravisaharan.criminalintent

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CrimeDetailFragmentTest {

    private lateinit var scenario: FragmentScenario<CrimeDetailFragment>
    @Before
    fun setUp() {
        scenario = launchFragmentInContainer<CrimeDetailFragment>()
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun editTextCrimeUpdate(){
        onView(withId(R.id.crime_title)).perform(typeText("Ghotala"), closeSoftKeyboard())

        scenario.onFragment { fragment ->
            assertEquals(fragment.crime.title, "Ghotala")
        }
    }

    @Test
    fun checkboxCrimeUpdate(){
        onView(withId(R.id.crime_solved)).perform(click())

        scenario.onFragment { fragment ->
            assertEquals(fragment.crime.isSolved, true)
        }
    }
}