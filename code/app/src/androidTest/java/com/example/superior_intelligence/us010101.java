package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
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

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the ability to add a mood event with required and optional fields,
 * ensuring the user can complete the flow and return to the home page.
 */
@RunWith(AndroidJUnit4.class)
public class us010101 {

    @Rule
    public GrantPermissionRule grantPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.READ_MEDIA_IMAGES);

    @Rule
    public IntentsTestRule<HomeActivity> homeActivityRule =
            new IntentsTestRule<>(HomeActivity.class, true, true);

    @Test
    public void testAddMoodEventFlow() throws InterruptedException {

        // Click add button to go to MoodCreateAndEditActivity
        onView(withId(R.id.addButton)).perform(click());
        intended(hasComponent(MoodCreateAndEditActivity.class.getName()));
        Thread.sleep(500);
        // Ensure we are on the MoodCreateAndEditActivity screen
        onView(withId(R.id.mood_event_title)).check(matches(isDisplayed()));
        Thread.sleep(500);
        // Enter the mood event title
        onView(withId(R.id.mood_event_title)).perform(replaceText("Feeling Great"));
        Thread.sleep(500);
        // Select an emotion from the dropdown
        onView(withId(R.id.emotion_arrow)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Happiness"))).perform(click());
        Thread.sleep(500);
        // Enter a trigger response (optional)
        onView(withId(R.id.trigger_response)).perform(replaceText("Had a great day!"));
        Thread.sleep(500);
        // Select a social situation (optional)
        onView(withId(R.id.situation_arrow)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Alone"))).perform(click());
        Thread.sleep(500);
        // Confirm mood event creation
        onView(withId(R.id.confirm_mood_create_button)).perform(click());
        Thread.sleep(500);
        // Ensure we are back on the HomeActivity screen
        intended(hasComponent(HomeActivity.class.getName()));
        Thread.sleep(500);
    }
}
