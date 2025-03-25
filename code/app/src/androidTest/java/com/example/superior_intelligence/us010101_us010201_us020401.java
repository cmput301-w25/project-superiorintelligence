package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.rule.ActivityTestRule;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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

public class us010101_us010201_us020401 {

    @Rule
    public ActivityTestRule<LoginPageActivity> loginRule =
            new ActivityTestRule<>(LoginPageActivity.class, true, false);

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Signs out any existing Firebase user, connects to the emulator, and ensures a test user exists.
     */
    @Before
    public void setup() throws InterruptedException {
        FirebaseAuth.getInstance().signOut();
        db.useEmulator("10.0.2.2", 8080);

        ensureUserExists("testUser", "Test User");
    }

    /**
     * End-to-end test that logs in as testUser, creates a complete mood post with:
     * title, emotion ("Happiness"), trigger text, and social situation ("Alone"),
     * then verifies the post appears in the MyPosts tab.
     */
    @Test
    public void testCreateMoodEventWithAllFields() throws InterruptedException {
        loginAs("testUser");

        onView(withId(R.id.addButton)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.mood_event_title)).perform(typeText("Test Mood"));
        androidx.test.espresso.Espresso.closeSoftKeyboard();

        // Open emotion spinner
        onView(withId(R.id.emotional_state_banner)).perform(click());

        // Select "happiness" from the dropdown dialog
        onView(withId(R.id.emotion_arrow)).perform(click());
        SystemClock.sleep(2000);
        onData(allOf(is(instanceOf(String.class)), is("Happiness"))).perform(click());

        onView(withId(R.id.selected_mood)).check(matches(withText("Happiness")));
        onView(withId(R.id.trigger_response)).perform(replaceText("Had a great day!"));
        Thread.sleep(500);

        onView(withId(R.id.situation_arrow)).perform(click());
        SystemClock.sleep(2000);
        onData(allOf(is(instanceOf(String.class)), is("Alone"))).perform(click());
        onView(withId(R.id.selected_situation)).check(matches(withText("Alone")));

        // Submit post
        onView(withId(R.id.confirm_mood_create_button)).perform(click());

        SystemClock.sleep(1000);
        onView(withId(R.id.public_checkbox)).perform(click());

        onView(withText("POST")).perform(click()); // or "CONFIRM" for editing
        SystemClock.sleep(3000);

        // Go to MyPosts to verify
        onView(withId(R.id.tab_myposts)).perform(click());
        SystemClock.sleep(1000);

        onView(withText("Test Mood")).check(matches(isDisplayed()));
    }

    /**
     * Logs in with the provided username using the login screen.
     * @param username the username to sign in as
     */
    private void loginAs(String username) throws InterruptedException {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginRule.launchActivity(intent);

        onView(withId(R.id.login_username)).perform(typeText(username));
        closeSoftKeyboard();
        onView(withId(R.id.login_button)).perform(click());
        SystemClock.sleep(3000);
    }

    /**
     * Ensures a user with the specified username and display name exists in the Firestore emulator.
     * @param username unique user ID
     * @param name     visible display name
     */
    private void ensureUserExists(String username, String name) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
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
     * Clears the emulated database.
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
