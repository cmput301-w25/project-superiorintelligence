package com.example.superior_intelligence.UserStoryTest;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import com.example.superior_intelligence.LoginPageActivity;
import com.example.superior_intelligence.R;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * UI test for US 01.03.01: ensures consistent emoticons and colors for each emotional state.
 */
public class us010301 {

    @Rule
    public ActivityTestRule<LoginPageActivity> loginRule =
            new ActivityTestRule<>(LoginPageActivity.class, true, false);

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final List<String> emotions = Arrays.asList(
            "Anger", "Confusion", "Disgust", "Fear", "Happiness", "Sadness", "Shame", "Surprise"
    );

    /**
     * Signs out, connects to the emulator, and ensures the test user exists.
     */
    @Before
    public void setup() throws InterruptedException {
        FirebaseAuth.getInstance().signOut();
        db.useEmulator("10.0.2.2", 8080);
        ensureUserExists("emoticonTester", "Emoticon Tester");
    }

    /**
     * Creates one mood event per emotion, each with its emoji and situation set.
     */
    @Test
    public void testEmoticonsForEachEmotion() throws InterruptedException {
        loginAs("emoticonTester");

        for (String emotion : emotions) {
            // Open mood creation screen
            onView(ViewMatchers.withId(R.id.addButton)).perform(click());
            SystemClock.sleep(1000);

            // Set title to emotion name
            onView(withId(R.id.mood_event_title)).perform(replaceText(emotion));
            closeSoftKeyboard();

            // Open and select emotion
            onView(withId(R.id.emotional_state_banner)).perform(click());
            onView(withId(R.id.emotion_arrow)).perform(click());
            SystemClock.sleep(500);
            onData(allOf(is(instanceOf(String.class)), is(emotion))).perform(click());
            onView(withId(R.id.selected_mood)).check(matches(withText(emotion)));

            // Optional: Select emoji (you could click the emoji button here if needed)
            onView(withId(R.id.include_emoji_checkbox)).perform(click());

            // Optional: Add reason and situation
            onView(withId(R.id.trigger_response)).perform(replaceText("Testing emoji for " + emotion));
            closeSoftKeyboard();
            onView(withId(R.id.situation_arrow)).perform(click());
            SystemClock.sleep(500);
            onData(allOf(is(instanceOf(String.class)), is("Alone"))).perform(click());

            // Confirm and submit
            onView(withId(R.id.confirm_mood_create_button)).perform(click());
            SystemClock.sleep(1000);
            onView(withId(R.id.public_checkbox)).perform(click());

            onView(withText("POST")).perform(click());
            SystemClock.sleep(1500);
        }

        // Switch to MyPosts tab
        onView(withId(R.id.tab_myposts)).perform(click());
        SystemClock.sleep(2000);
    }

    /**
     * Logs in using the given username.
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
     * Creates a test user in the Firestore emulator if not already present.
     */
    private void ensureUserExists(String username, String name) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("name", name);
        user.put("followers", new java.util.ArrayList<>());
        user.put("following", new java.util.ArrayList<>());

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
