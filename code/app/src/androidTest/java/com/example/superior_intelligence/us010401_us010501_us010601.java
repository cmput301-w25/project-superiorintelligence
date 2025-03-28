package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * Espresso UI test for:
 * - Viewing a mood event (US 01.04.01)
 * - Editing a mood event (US 01.05.01)
 * - Deleting a mood event (US 01.06.01)
 * Covers creation of an event, editing mood/reason/title, and deletion verification.
 */
public class us010401_us010501_us010601 {

    @Rule
    public ActivityTestRule<LoginPageActivity> loginRule =
            new ActivityTestRule<>(LoginPageActivity.class, true, false);

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Signs out any user and ensures test user exists in Firestore before each test.
     */
    @Before
    public void setup() throws InterruptedException {
        FirebaseAuth.getInstance().signOut();
        db.useEmulator("10.0.2.2", 8080);
        ensureUserExists("testUser", "Test User", "TestPass");
        Intents.init();
    }

    /**
     * Clears Firestore emulator database after each test to ensure clean slate.
     */
    @After
    public void tearDown() {
        String projectId = "moodgram";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * Tests the full flow:
     * 1. Create a new mood event.
     * 2. Edit the event title, mood, and reason.
     * 3. Verify the changes in detail view.
     * 4. Delete the event and verify it is removed.
     */
    @Test
    public void testViewEditDeleteMoodEvent() throws InterruptedException {
        loginAs("testUser","TestPass");

        SystemClock.sleep(3000);

        onView(withId(R.id.addButton)).perform(click());
        onView(withId(R.id.mood_event_title)).perform(typeText("Original Title"));
        closeSoftKeyboard();

        onView(withId(R.id.emotion_arrow)).perform(click());
        SystemClock.sleep(1000);
        onData(Matchers.allOf(is(instanceOf(String.class)), is("Happiness"))).perform(click());

        onView(withId(R.id.trigger_response)).perform(typeText("Original Reason"));
        closeSoftKeyboard();

        onView(withId(R.id.situation_arrow)).perform(click());
        SystemClock.sleep(1000);
        onData(Matchers.allOf(is(instanceOf(String.class)), is("Alone"))).perform(click());
        onView(withId(R.id.selected_situation)).check(matches(withText("Alone")));

        onView(withId(R.id.confirm_mood_create_button)).perform(click());
        onView(withId(R.id.public_checkbox)).perform(click());
        onView(anyOf(withText("CONFIRM"), withText("POST")))
                .check(matches(isDisplayed()))
                .perform(click());

        SystemClock.sleep(3000);

        onView(withId(R.id.tab_myposts)).perform(click());
        SystemClock.sleep(1000);
        onView(withText("Original Title")).perform(click());
        onView(withId(R.id.event_detail_title)).check(matches(withText("Original Title")));


        onView(withId(R.id.edit_button)).perform(click());
        onView(withId(R.id.mood_event_title)).perform(replaceText("Edited Title"));
        closeSoftKeyboard();

        onView(withId(R.id.emotion_arrow)).perform(click());
        SystemClock.sleep(1000);
        onData(Matchers.allOf(is(instanceOf(String.class)), is("Anger"))).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.selected_mood)).check(matches(withText("Anger")));

        onView(withId(R.id.trigger_response)).perform(replaceText("Updated Reason"));
        closeSoftKeyboard();

        onView(withId(R.id.confirm_mood_create_button)).perform(click());
        SystemClock.sleep(3000);

        onView(withId(R.id.tab_myposts)).perform(click());
        SystemClock.sleep(1000);
        onView(withText("Edited Title")).check(matches(isDisplayed()));
        SystemClock.sleep(3000);

        onView(withText("Edited Title")).perform(click());
        SystemClock.sleep(3000);
        onView(withId(R.id.event_detail_title)).check(matches(withText("Edited Title")));
        onView(withId(R.id.selected_mood)).check(matches(withText("Anger")));
        onView(withId(R.id.event_detail_reason)).check(matches(withText("Reason: Updated Reason")));

        onView(withId(R.id.delete_button)).perform(click());
        SystemClock.sleep(3000);
        onView(withText("DELETE")).perform(click());

        SystemClock.sleep(3000);

        onView(withText("Edited Title")).check(doesNotExist());
    }

    /**
     * Helper method to log in as the given test user.
     * @param username Firebase Auth test username
     */
    private void loginAs(String username, String password) throws InterruptedException {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginRule.launchActivity(intent);

        onView(withId(R.id.login_username)).perform(typeText(username));
        ViewActions.closeSoftKeyboard();

        onView(withId(R.id.login_password)).perform(typeText(password));
        ViewActions.closeSoftKeyboard();

        onView(withId(R.id.login_button)).perform(click());
        SystemClock.sleep(3000);
    }

    /**
     * Ensures the test user exists in Firestore before running tests.
     * @param username Desired username
     * @param name     Display name
     */
    private void ensureUserExists(String username, String name, String rawPassword) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("name", name);
        user.put("followers", new java.util.ArrayList<>());
        user.put("following", new java.util.ArrayList<>());

        String hashedPassword = PasswordHasher.hashPassword(rawPassword);
        user.put("password", hashedPassword);

        db.collection("users").document(username).set(user)
                .addOnCompleteListener(task -> latch.countDown());

        latch.await();
    }
}
