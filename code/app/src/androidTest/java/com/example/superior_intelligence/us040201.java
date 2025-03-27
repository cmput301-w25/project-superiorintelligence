package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertTrue;
import static java.util.Calendar.SECOND;
import static java.util.regex.Pattern.matches;

import android.graphics.Movie;
import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

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
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(JUnit4.class)
public class us040201 {

    /**
     * Rule to start test from HomeActivity class
     */
    // Start at HomeActivity
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);


    /**
     * Test that filtering recent week should not include post before that week
     */

    @Test
    public void filterRecentWeekInMyPostsShouldNotDisplayOnlyValidEvent() throws InterruptedException {
        //Ensure that test is on myPosts tab
        onView(withId(R.id.tab_myposts)).perform(click());

        onView(withText("Test during week")).check(ViewAssertions.matches(isDisplayed()));

        //click on menu button to select filter
        onView(withId(R.id.menu_button)).perform(click());

        //click on filter to see last 7 days post
        onView(withText("Show posts from last 7 days")).perform(click());

        //test that created last year should not appear
        onView(withText("Test during week")).check(doesNotExist());

    }
    /**
     * Add events that needed in the testing to the database
     */
    @Before // run before every test
    public void setUpDB() throws InterruptedException {
        /*
        Database db = new Database();
        CollectionReference postsRef = db.getMyPostsRef();
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        Event event = new Event("TestBeforeRecWk", "Test before recent week", "12 MAR 2024, 12:04", "CC0099", "", 0, false, true, "Surprise", "", "", "testUser3", null, null, true);
        postsRef.document(event.getID()).set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Event successfully added");
                    latch.countDown(); // Release the lock
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to add event", e);
                    latch.countDown(); // Release the lock even on failure
                });

        latch.await(); // Wait for Firestore operation to complete before proceeding
        */
        logIn();

        createEvents();

        // log in
        /*
        Event[] events = {
                new Event("TestBeforeRecWk", "Test before recent week", "12 MAR 2024, 12:04", "CC0099", "", 0, false, true, "Surprise", "", "", "moodgramGrace", null, null, true),
                //new Event("TestDuringRecWk", "Test during recent week", "24 MAR 2025, 12:04", "CC0099", "", 0, false, true, "Surprise", "", "", "moodgramGrace", null, null, true)
        };

        for (Event mood : events) {
            postsRef.document(mood.getID()).set(mood)
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Event added: " + mood.getID()))
                    .addOnFailureListener(e -> Log.e("Firestore", "Failed to add event", e));
        }
        Thread.sleep(12000);
         */

    }

    /**
     * Log in to the homepage using testUser3
     * @throws InterruptedException
     */
    public void logIn() throws InterruptedException {
        onView(withId(R.id.login_button_login_page)).perform(click());
        onView(withId(R.id.signup_page_button)).perform(click());
        onView(withId(R.id.signup_name)).perform(typeText("TestUser"));
        onView(withId(R.id.signup_username)).perform(typeText("testUser3")).perform(closeSoftKeyboard());
        Thread.sleep(2000);
        onView(withId(R.id.signup_button)).perform(click());
        Thread.sleep(5000);

        onView(withId(R.id.login_username)).perform(typeText("testUser3"));
        onView(withId(R.id.login_button)).perform(click());
        Thread.sleep(12000);

    }

    /**
     * Create event for during the week
     */
    public void createEvents() throws InterruptedException {
        onView(withId(R.id.addButton)).perform(click());
        onView(withId(R.id.mood_event_title)).perform(typeText("Test during week")).perform(closeSoftKeyboard());
        onView(withId(R.id.emotion_arrow)).perform(click());
        onView(withText("Anger")).perform(click());
        onView(withId(R.id.confirm_mood_create_button)).perform(click());
        onView(withText("POST")).perform(click());
        Thread.sleep(5000);
    }

    public void createLastYearEvent(){
        onView(withId(R.id.addButton)).perform(click());
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

    // only run once
    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }
}
