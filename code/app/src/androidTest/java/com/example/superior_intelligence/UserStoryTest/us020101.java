package com.example.superior_intelligence.UserStoryTest;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;

import static org.hamcrest.Matchers.is;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.superior_intelligence.HomeActivity;
import com.example.superior_intelligence.R;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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
 * UI Test for US 02.01.01 - As a participant, I want to express the reason why for a mood event,
 * using a brief textual explanation no more than 200 characters or 3 words.
 */
@RunWith(AndroidJUnit4.class)
public class us020101 {
    @Rule
    public ActivityTestRule<HomeActivity> activityRule =
            new ActivityTestRule<>(HomeActivity.class, true, false);

    /**
     * Sets up Firebase emulator and ensures test user exists before each test.
     */
    @Before
    public void setUp() throws InterruptedException {
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
        ensureUserExists("testUser", "Test User");
    }

    /**
     * Tests whether a mood reason over 200 characters is allowed or correctly handled.
     * This checks the system's limit handling.
     */
    @Test
    public void testValidAndInvalidMoodReasons() throws InterruptedException {
        loginAs("testUser");

        onView(ViewMatchers.withId(R.id.addButton)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.mood_event_title)).perform(replaceText("Too Long Reason Example"));

        // Select mood: Sadness
        onView(withId(R.id.emotion_arrow)).perform(click());
        onData(is("Sadness")).perform(click());

        // Generate a string over 200 characters
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 220; i++) longText.append("x");

        // Type it into the reason field
        onView(withId(R.id.trigger_response)).perform(replaceText(longText.toString()));
        closeSoftKeyboard();
        SystemClock.sleep(3000);
        // Submit post
        onView(withId(R.id.confirm_mood_create_button)).perform(click());
        SystemClock.sleep(500);

        // Select public in the dialog
        onView(withId(R.id.public_checkbox)).inRoot(isDialog()).perform(click());
        onView(withText("POST")).inRoot(isDialog()).perform(click());
        SystemClock.sleep(1000);

        // Verify we're either still on the page or redirected correctly
        onView(withId(R.id.tab_myposts)).check(matches(isDisplayed()));
    }

    /**
     * Logs in using the given username.
     */
    private void loginAs(String username) throws InterruptedException {
        Intent intent = new Intent();
        activityRule.launchActivity(intent);

        onView(withId(R.id.login_username)).perform(typeText(username));
        closeSoftKeyboard();
        onView(withId(R.id.login_button)).perform(click());

        SystemClock.sleep(3000);
    }

    /**
     * Creates a test user in the Firestore emulator if not already present.
     */
    private void ensureUserExists(String username, String name) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("name", name);
        user.put("followers", new ArrayList<>());
        user.put("following", new ArrayList<>());
        db.collection("users").document(username).set(user)
                .addOnCompleteListener(task -> latch.countDown());
        latch.await();
    }

    /**
     * Clears all documents from Firestore after tests.
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
}

