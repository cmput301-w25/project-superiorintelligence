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
import android.icu.text.SimpleDateFormat;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class us050101andus050102 {

    // 1) TELL JUnit NOT to auto-start the Activity: launchActivity=false
    @Rule
    public ActivityTestRule<LoginPageActivity> loginRule =
            new ActivityTestRule<>(LoginPageActivity.class, /*initialTouchMode=*/true, /*launchActivity=*/false);

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String projectId = "moodgram";

    /**
     * Sets up emulator, test users, and a sample public event before each test.
     * Also confirms that required data was written to the emulator.
     */
    @Before
    public void setup() throws InterruptedException {
        // Sign out any old user
        FirebaseAuth.getInstance().signOut();
        // Ensure we're using the local emulator, not production
        db.useEmulator("10.0.2.2", 8080);

        // (Optional) Wipe everything if you want a fresh slate each time
        wipeEmulator();

        // Now create all the test data
        ensureUserExists("userA", "Alice A", "pass1");
        ensureUserExists("userB", "Bob B", "pass2");
        ensureEventByUserAExists();

        // Confirm the docs actually exist before we let the test proceed
        confirmUserExists("userA");
        confirmUserExists("userB");
        confirmEventExists("userA_event");
        Intents.init();
    }

    /**
     * Tests that userB can request to follow userA, sees a pending notification,
     * and userA receives the request and sees it in their notification center.
     */
    @Test
    public void testFollowRequestAndAcceptFlow() throws InterruptedException {
        // *** Now we manually launch the Activity after data is ready ***
        Intent intent = new Intent();
        // Clears any old tasks so we start fresh
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginRule.launchActivity(intent);  // Finally start LoginPageActivity

        // Then do the usual login steps
        onView(withId(R.id.login_username)).perform(typeText("userB"));
        closeSoftKeyboard();
        onView(withId(R.id.login_button)).perform(click());
        SystemClock.sleep(3000);

        // The rest is the same
        onView(withId(R.id.tab_explore)).check(matches(isDisplayed()));
        onView(withId(R.id.tab_explore)).perform(click());
        SystemClock.sleep(2000);

        onView(withText("Title by userA")).perform(click());

        // Go to userA's profile
        onView(withId(R.id.profile_image)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.follow_request_button)).check(matches(isDisplayed()));

        SystemClock.sleep(1000);
        onView(withId(R.id.follow_request_button)).perform(click());
        SystemClock.sleep(1000);

        pressBack();
        pressBack();

        // Check we can see "Pending requests"
        onView(withId(R.id.notification_button)).perform(click());
        onView(withText("Pending requests")).perform(click());
        SystemClock.sleep(1000);

        onView(withText("You requested to follow userA. Pending approval."))
                .check(matches(isDisplayed()));

        // Sign out
        pressBack();
        onView(withId(R.id.profile_image)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.signout)).perform(click());

        SystemClock.sleep(1000);

        // Log back in as userA
        loginAs("userA", "pass1");
        onView(withId(R.id.notification_button)).perform(click());
        onView(withText("userB wants to follow you."))
                .check(matches(isDisplayed()));

        SystemClock.sleep(2000);
    }

    /**
     * A shorter "loginAs(...)" method that re-launches the activity.
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
     * Ensure user doc is created
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
     * Ensure event doc is created
     */
    private void ensureEventByUserAExists() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String eventId = "userA_event";
        String formattedNow = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date());
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
        event.put("date", formattedNow);
        event.put("public_status", true);
        event.put("comments", new HashMap<>());
        event.put("id", eventId);

        db.collection("MyPosts").document(eventId).set(event)
                .addOnCompleteListener(task -> latch.countDown());

        latch.await();
    }

    /**
     * Confirm user doc is present
     */
    private void confirmUserExists(String username) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        db.collection("users").document(username).get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                        throw new AssertionError("User doc " + username + " not found in emulator!");
                    }
                    Log.d("ConfirmUserExists", "User " + username + " found!");
                    latch.countDown();
                });
        latch.await();
    }

    /**
     * Confirm event doc is present
     */
    private void confirmEventExists(String eventId) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        db.collection("MyPosts").document(eventId).get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                        throw new AssertionError("Event doc " + eventId + " not found in emulator!");
                    }
                    Log.d("ConfirmEventExists", "Event " + eventId + " found!");
                    latch.countDown();
                });
        latch.await();
    }

    /**
     * Wipes the emulator after each test
     */
    @After
    public void tearDown() {
        wipeEmulator();
    }

    /**
     * Helper method to remove all docs from the local emulator
     */
    private void wipeEmulator() {
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
