package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static java.lang.Thread.sleep;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class us020401 {

    @Rule
    public ActivityScenarioRule<HomeActivity> homeRule =
            new ActivityScenarioRule<>(HomeActivity.class);

    @Test
    public void testShowSituationList() throws InterruptedException {

        // Ensure we are in HomeActivity
        onView(withId(R.id.addButton)).check(matches(isDisplayed()));

        // Click the add button to navigate to MoodCreateAndEditActivity
        onView(withId(R.id.addButton)).perform(click());
        onView(withId(R.id.mood_event_title)).check(matches(isDisplayed()));

        // Click the situation dropdown
        onView(withId(R.id.situation_arrow)).perform(click());

        // Pause to show the situation list
        sleep(3000);

        // Verify that expected situations are displayed
        onView(withText("Alone")).check(matches(isDisplayed()));
        onView(withText("With one other person")).check(matches(isDisplayed()));
        onView(withText("With two to several people")).check(matches(isDisplayed()));
        onView(withText("With a crowd")).check(matches(isDisplayed()));

        // Additional pause for user observation
        sleep(3000);
    }
}