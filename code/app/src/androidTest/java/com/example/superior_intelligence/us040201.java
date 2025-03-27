package com.example.superior_intelligence;
/**
 * Test filtering recent week for my posts
 */

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertTrue;
import static java.util.Calendar.SECOND;
import static java.util.regex.Pattern.matches;

import android.graphics.Movie;
import android.util.Log;
import android.widget.DatePicker;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.hamcrest.Matchers;
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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
     * Test that filtering recent week should not include my post before recent week
     * @throws InterruptedException
     */

    @Test
    public void filterRecentWeekInMyPostsShouldNotDisplayEventOutsideRecentWeek() throws InterruptedException {
        //Ensure that test is on myPosts tab
        onView(withId(R.id.tab_myposts)).perform(click());

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
     * Test that filtering recent week should include my post during the recent week
     * @throws InterruptedException
     */
    @Test
    public void filterRecentWeekInMyPostsShouldDisplayEventDuringRecentWeek() throws InterruptedException {
        //Ensure that test is on myPosts tab
        onView(withId(R.id.tab_myposts)).perform(click());

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
        logIn();

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
        Thread.sleep(5000);

    }

    /**
     * Add my posts to database with date before recent week and during recent week
     * @throws InterruptedException
     */
    public void seedMyPostDB() throws InterruptedException {
        Database db = new Database();
        CollectionReference postsRef = db.getMyPostsRef();
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        Event event = new Event("TestBeforeRecWk", "Test last year", "12 MAR 2024, 12:04", "#CC0099", "", 0, false, true, "Surprise", "", "", "testUser3", null, null, true);
        Event event2 = new Event("TestDuringRecWk", "Test during recent week", "27 MAR 2025, 12:04", "#FF6347", "", 0, false, true, "Anger", "", "", "testUser3", null, null, true);
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

    // only run once
    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }
}
