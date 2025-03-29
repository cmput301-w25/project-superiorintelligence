package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.action.ViewActions;
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

public class us010701 {
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

        ensureUserExists("testUser", "Test User", "pass1");
        ensureUserExists("randomUser", "Random User", "pass2");
    }

    @Test
    public void testCreatingPublicPrivateEvents() throws InterruptedException {
        loginAs("testUser", "pass1");

        onView(withId(R.id.addButton)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.mood_event_title)).perform(typeText("Test Public"));
        androidx.test.espresso.Espresso.closeSoftKeyboard();

        onView(withId(R.id.emotion_arrow)).perform(click());
        SystemClock.sleep(2000);
        onData(allOf(is(instanceOf(String.class)), is("Happiness"))).perform(click());

        onView(withId(R.id.trigger_response)).perform(replaceText("For Everyone To See!"));
        Thread.sleep(500);

        // Submit post
        onView(withId(R.id.confirm_mood_create_button)).perform(click());

        SystemClock.sleep(1000);
        onView(withId(R.id.public_checkbox)).perform(click());

        onView(withText("POST")).perform(click());
        SystemClock.sleep(3000);

        // Go to MyPosts to verify
        onView(withId(R.id.tab_myposts)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.addButton)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.mood_event_title)).perform(typeText("Test Private"));
        androidx.test.espresso.Espresso.closeSoftKeyboard();

        onView(withId(R.id.emotion_arrow)).perform(click());
        SystemClock.sleep(2000);
        onData(allOf(is(instanceOf(String.class)), is("Happiness"))).perform(click());

        onView(withId(R.id.trigger_response)).perform(replaceText("For Only Me!"));
        Thread.sleep(500);

        // Submit post
        onView(withId(R.id.confirm_mood_create_button)).perform(click());

        SystemClock.sleep(1000);
        onView(withId(R.id.public_checkbox)).perform(click());

        onView(withText("POST")).perform(click());
        SystemClock.sleep(3000);

        // Go to MyPosts to verify
        onView(withId(R.id.tab_myposts)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.profile_image)).perform(click());
        SystemClock.sleep(2000);

        onView(withId(R.id.signout)).perform(click());

        SystemClock.sleep(1000);

        loginAs("randomUser", "pass2");

    }

    /**
     * Logs in using the given username.
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
     * Creates a test user in the Firestore emulator if not already present.
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
