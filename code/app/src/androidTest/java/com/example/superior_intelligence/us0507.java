package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.auth.FirebaseAuth;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Test class to verify user stories 05.07.01 and 05.07.02.
 * Ensures participants can comment on a mood event and view all comments.
 */
@RunWith(AndroidJUnit4.class)
public class us0507 {

    @Rule
    public ActivityTestRule<HomeActivity> activityRule =
            new ActivityTestRule<>(HomeActivity.class, true, false);

    /**
     * Sets up the test environment by connecting to the Firestore emulator,
     * signing in anonymously, setting a test username, and creating a test event.
     * @throws InterruptedException if interrupted during setup
     */
    @Before
    public void setUp() throws InterruptedException {
        // Force Firestore to use the emulator.
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.useEmulator("10.0.2.2", 8080);

        // Sign in anonymously so HomeActivity doesn't redirect to LoginPageActivity.
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final CountDownLatch authLatch = new CountDownLatch(1);
        auth.signInAnonymously().addOnCompleteListener(task -> authLatch.countDown());
        authLatch.await();

        // Set a valid username in your User singleton.
        User.getInstance().setUsername("testUser");

        activityRule.launchActivity(new Intent());

        createTestEventNotOwnedByUser();

        SystemClock.sleep(3000);
    }

    /**
     * Helper method to create a mood event owned by a different user ("otherUser")
     * so that the test user can comment on it.
     * @throws InterruptedException if interrupted while waiting for Firestore
     */
    private void createTestEventNotOwnedByUser() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        Event testEvent = new Event();
        testEvent.setID(UUID.randomUUID().toString());
        testEvent.setTitle("Test Other User Event");
        testEvent.setDate(new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date()));
        testEvent.setOverlayColor("#FFD700");
        testEvent.setImageUrl("");
        testEvent.setEmojiResource(R.drawable.happy_icon);
        testEvent.setFollowed(false);
        testEvent.setMyPost(false);
        testEvent.setMood("Happiness");
        testEvent.setMoodExplanation("This is a test event from another user");
        testEvent.setSituation("Alone");
        testEvent.setUser("otherUser");
        testEvent.setLat(0.0);
        testEvent.setLng(0.0);
        testEvent.setPublic_status(true);

        Database.getInstance().saveEventToFirebase(testEvent, success -> latch.countDown());
        latch.await();
    }

    /**
     * Tests commenting functionality on another user's mood event.
     * @throws InterruptedException if interrupted during UI interaction
     */
    @Test
    public void testCommentOnOtherUserEvent() throws InterruptedException {
        onView(withId(R.id.tab_explore)).perform(click());
        SystemClock.sleep(1000);

        onView(withText("Test Other User Event")).perform(click());

        String testComment = "Automated test comment on other user's event!";
        onView(withId(R.id.comment_input))
                .perform(typeText(testComment), closeSoftKeyboard());

        onView(withId(R.id.send_comment_button)).perform(click());

        SystemClock.sleep(2000);

        onView(withId(R.id.comments_recycler_view))
                .check(matches(hasDescendant(withText(testComment))));

        pressBack();

        onView(withId(R.id.tab_explore)).perform(click());
        SystemClock.sleep(1000);

        onView(withText("Test Other User Event")).perform(click());

        onView(withId(R.id.comments_recycler_view))
                .check(matches(hasDescendant(withText(testComment))));

        SystemClock.sleep(1000);
    }


    /**
     * Cleans up the Firestore emulator by deleting all data after the test execution.
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
