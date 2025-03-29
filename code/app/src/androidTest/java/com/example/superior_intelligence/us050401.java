package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.util.Log;

import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

@RunWith(JUnit4.class)
public class us050401 {

    private String loggedInUserName;

    // Start at HomeActivity
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Test that filtering recent week should not include followed post before recent week
     * @throws InterruptedException
     */

    @Test
    public void filterRecentWeekInFollowedShouldNotDisplayEventOutsideRecentWeek() throws InterruptedException {
        //Ensure that test is on myPosts tab
        onView(withId(R.id.tab_followed)).perform(click());
        Thread.sleep(5000);
        //Make sure added events are displayed
        onView(withText("Test during recent week")).check(ViewAssertions.matches(isDisplayed()));
        onView(withText("Test last year")).check(ViewAssertions.matches(isDisplayed()));

        //click on menu button to select filter
        onView(withId(R.id.menu_button)).perform(click());

        //click on filter to see last 7 days post
        onView(withId(R.id.recent_week_option)).perform(click());

        //test that created last year should not appear
        onView(withText("Test last year")).check(doesNotExist());

    }

    /**
     * Test that filtering recent week should include followed during the recent week
     * @throws InterruptedException
     */
    @Test
    public void filterRecentWeekInFollowedShouldDisplayEventDuringRecentWeek() throws InterruptedException {
        //Ensure that test is on myPosts tab
        onView(withId(R.id.tab_followed)).perform(click());

        //Make sure added events are displayed
        onView(withText("Test during recent week")).check(ViewAssertions.matches(isDisplayed()));
        onView(withText("Test last year")).check(ViewAssertions.matches(isDisplayed()));

        //click on menu button to select filter
        onView(withId(R.id.menu_button)).perform(click());

        //click on filter to see last 7 days post
        onView(withId(R.id.recent_week_option)).perform(click());

        //test that created last year should not appear
        onView(withText("Test during recent week")).check(ViewAssertions.matches(isDisplayed()));

    }


    /**
     * Add events that needed in the testing to the database
     * Log in to start the test
     * @throws InterruptedException
     */
    @Before // run before every test
    public void setUp() throws InterruptedException {
        seedMyPostDB();
        seedUserDB();
        logIn();
    }

    /**
     * Log in to the homepage using testUser3
     * @throws InterruptedException
     */
    public void logIn() throws InterruptedException {
        onView(withId(R.id.login_button_login_page)).perform(click());

        onView(withId(R.id.login_username)).perform(typeText("followedUser")).perform(closeSoftKeyboard());
        onView(withId(R.id.login_password)).perform(typeText("TestPasss")).perform(closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        Thread.sleep(5000);
    }

    /**
     * Set up user and its following in the database
     */
    private void seedUserDB() throws InterruptedException {
        String username = "testUser", name = "Test User", rawPassword = "TestPass";
        String username2 = "followedUser", name2 = "Test User Logged In", rawPassword2 = "TestPasss";
        seedUser(username, name, rawPassword);
        seedUser(username2, name2, rawPassword2);

        followUser(username2, username, success -> {
            Log.d("FollowDebug", "followedUser successfully follow testUser");
        });

    }

    /**
     * Add a user to the database
     * @param username
     * @param name
     * @param rawPassword
     * @throws InterruptedException
     */
    public void seedUser(String username, String name, String rawPassword) throws InterruptedException {
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

    public void followUser(String followingUser, String followedUser, Userbase.FollowActionCallback callback) throws InterruptedException {
        db.collection("users").document(followingUser)
                .update("following", FieldValue.arrayUnion(followedUser))
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(followedUser)
                            .update("followers", FieldValue.arrayUnion(followingUser))
                            .addOnSuccessListener(aVoid2 -> callback.onFollowAction(true))
                            .addOnFailureListener(e -> callback.onFollowAction(false));
                })
                .addOnFailureListener(e -> callback.onFollowAction(false));
    }

    /**
     * Add my posts to database with date before recent week and during recent week
     * @throws InterruptedException
     */
    public void seedMyPostDB() throws InterruptedException {
        Database db = new Database();
        CollectionReference postsRef = db.getMyPostsRef();
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        Event event = new Event("TestBeforeRecWk", "Test last year", "12 MAR 2024, 12:04", "#CC0099", "", 0, true, false, "Surprise", "", "", "testUser", null, null, true);
        Event event2 = new Event("TestDuringRecWk", "Test during recent week", null, "#FF6347", "", 0, true, false, "Anger", "", "", "testUser", null, null, true);

        // current date to ensure test will work regardless of the time
        String eventDate = new SimpleDateFormat("dd MMM yyyy, HH:mm",
                Locale.getDefault()).format(new Date());
        event2.setDate(eventDate);

        postsRef.document(event.getID()).set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Event successfully added");
                    latch.countDown(); // Release the lock
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to add event", e);
                    latch.countDown(); // Release the lock even on failure
                });
        postsRef.document(event2.getID()).set(event2)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Event successfully added");
                    latch.countDown(); // Release the lock
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to add event", e);
                    latch.countDown(); // Release the lock even on failure
                });
        latch.await();
    }


    /**
     * Remove any of the data that were added in the beginning of the test
     */
    @After // run after every test
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
     * Set up emulators before the test class is run
     */
    // only run once
    @BeforeClass
    public static void setupEmulator(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

}
