package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.Manifest;
import android.content.Intent;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the ability to add a mood event with required and optional fields,
 * ensuring the user can complete the flow and return to the home page.
 */
@RunWith(AndroidJUnit4.class)
public class us010201 {

    @Rule
    public ActivityScenarioRule<MoodCreateAndEditActivity> moodCreateRule =
            new ActivityScenarioRule<>(MoodCreateAndEditActivity.class);

    @Before
    public void setup() {
        Intents.init();
    }

    @Test
    public void testEmotionalStatesList() throws InterruptedException {
        // Ensure we are on the MoodCreateAndEditActivity screen
        onView(withId(R.id.mood_event_title)).check(matches(isDisplayed()));

        // Click the emotion selection dropdown
        onView(withId(R.id.emotion_arrow)).perform(click());

        // Pause for a few seconds to display all available emotions
        Thread.sleep(3000); // 3-second pause for visibility

        // Check that all required emotions are present
        onData(allOf(is(instanceOf(String.class)), is("Anger"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("Confusion"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("Disgust"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("Fear"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("Happiness"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("Sadness"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("Shame"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("Surprise"))).check(matches(isDisplayed()));
    }
}