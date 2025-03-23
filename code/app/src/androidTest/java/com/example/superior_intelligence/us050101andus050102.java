package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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

public class us050101andus050102 {

    @Rule
    public ActivityTestRule<LoginPageActivity> loginRule =
            new ActivityTestRule<>(LoginPageActivity.class, true, false);

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Setup method executed before each test. Signs out existing users,
     * connects to the Firestore emulator, and creates mock users and a mood event.
     */
    @Before
    public void setup() throws InterruptedException {
        FirebaseAuth.getInstance().signOut();
        db.useEmulator("10.0.2.2", 8080);

        // Create users and an event for userA
        ensureUserExists("userA", "Alice A");
        ensureUserExists("userB", "Bob B");
        ensureEventByUserAExists(); // Preload into MyPosts
    }

    /**
     * End-to-end test covering the flow of userB requesting to follow userA,
     * and userA seeing the follow request in their notifications.
     */
    @Test
    public void testFollowRequestAndAcceptFlow() throws InterruptedException {
        loginAs("userB");

        onView(withId(R.id.tab_explore)).check(matches(isDisplayed()));

        // Click on explore tab if not selected already
        onView(withId(R.id.tab_explore)).perform(click());
        SystemClock.sleep(2000);

        onView(withText("Title by userA")).perform(click());

        // Click on profile icon to go to userA's profile
        onView(withId(R.id.profile_image)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.follow_request_button))
                .check(matches(isDisplayed()));

        SystemClock.sleep(1000);

        onView(withId(R.id.follow_request_button)).perform(click());

        SystemClock.sleep(1000);

        pressBack();
        pressBack();


        onView(withId(R.id.notification_button)).perform(click());


        onView(withText("Pending requests")).perform(click());
        SystemClock.sleep(1000);

        // Check if pending request is displayed
        onView(withText("You requested to follow userA. Pending approval.")).check(matches(isDisplayed()));

        // Return to Home, go to profile, log out
        pressBack();
        onView(withId(R.id.profile_image)).perform(click());
        SystemClock.sleep(2000);

        onView(withId(R.id.signout)).perform(click());

        SystemClock.sleep(1000);

        loginAs("userA");

        // Open notifications and check for incoming
        onView(withId(R.id.notification_button)).perform(click());
        onView(withText("userB wants to follow you.")).check(matches(isDisplayed()));

        SystemClock.sleep(2000);
    }

    /**
     * Logs in to the app as the specified user.
     * @param username the username to log in with
     */
    private void loginAs(String username) throws InterruptedException {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 💥 clears back stack
        loginRule.launchActivity(intent);

        onView(withId(R.id.login_username)).perform(typeText(username));
        closeSoftKeyboard();
        onView(withId(R.id.login_button)).perform(click());
        SystemClock.sleep(3000); // Wait for HomeActivity to load
    }

    /**
     * Creates a user document in Firestore.
     * @param username the user's username
     * @param name     the display name of the user
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
     * Creates a mock event posted by userA for display in the Explore feed.
     */
    private void ensureEventByUserAExists() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String eventId = "userA_event";

        Map<String, Object> event = new HashMap<>();
        event.put("title", "Title by userA");
        event.put("postUser", "userA");
        event.put("isMyPost", false);
        event.put("isFollowed", false);
        event.put("overlayColor", "#FFD700");
        event.put("mood", "happiness");
        event.put("situation", "Alone");
        event.put("moodExplanation", "Just vibing");
        event.put("emojiResource", R.drawable.happy_icon);
        event.put("dateTime", com.google.firebase.Timestamp.now());
        event.put("comments", new HashMap<>());

        // Add the ID field explicitly so it's available in EventDetailsActivity
        event.put("id", eventId);

        db.collection("MyPosts").document(eventId).set(event)
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

