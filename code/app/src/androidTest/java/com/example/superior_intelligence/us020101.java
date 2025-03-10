package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.content.Intent;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class us020101 {

    @Rule
    public IntentsTestRule<HomeActivity> homeActivityRule =
            new IntentsTestRule<>(HomeActivity.class, true, true);

    @Test
    public void testTriggerResponseValidation() throws InterruptedException {
        // Click Add Mood Event button
        onView(withId(R.id.addButton)).perform(click());
        intended(hasComponent(MoodCreateAndEditActivity.class.getName()));

        // Enter a valid title
        onView(withId(R.id.mood_event_title))
                .perform(replaceText("Test Mood Event"), closeSoftKeyboard());
        Thread.sleep(1000); // Pause to observe

        // Select an emotion
        onView(withId(R.id.emotion_arrow)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Happiness"))).perform(click());
        Thread.sleep(1000); // Pause to observe

        // Enter a **valid** trigger response (≤ 20 chars or ≤ 3 words)
        onView(withId(R.id.trigger_response))
                .perform(replaceText("Short reason"), closeSoftKeyboard());
        Thread.sleep(1000); // Pause to observe

        // Click Confirm button
        onView(withId(R.id.confirm_mood_create_button)).perform(click());

        // Expect to navigate to HomeActivity
        intended(hasComponent(HomeActivity.class.getName()));
    }

    @Test
    public void testInvalidTriggerResponse() throws InterruptedException {
        // Click Add Mood Event button
        onView(withId(R.id.addButton)).perform(click());
        intended(hasComponent(MoodCreateAndEditActivity.class.getName()));

        // Enter a valid title
        onView(withId(R.id.mood_event_title))
                .perform(replaceText("Test Mood Event"), closeSoftKeyboard());
        Thread.sleep(1000); // Pause to observe

        // Select an emotion
        onView(withId(R.id.emotion_arrow)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Happiness"))).perform(click());
        Thread.sleep(1000); // Pause to observe

        // Enter an **invalid** trigger response (> 20 chars or > 3 words)
        onView(withId(R.id.trigger_response))
                .perform(replaceText("This is an extremely long reason"), closeSoftKeyboard());
        Thread.sleep(1000); // Pause to observe

        // Click Confirm button
        onView(withId(R.id.confirm_mood_create_button)).perform(click());

        // Verify that we are still in MoodCreateAndEditActivity (error should prevent navigation)
        onView(withId(R.id.mood_event_title)).check(matches(isDisplayed()));
    }
}