package com.example.superior_intelligence;
/**
 * Test filtering show 3 most recent posts from followed posts
 */

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertEquals;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

@RunWith(JUnit4.class)
public class us050301 {
    // Start at HomeActivity
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    /**
     * Test filtering show 3 most recent posts should only display 3 posts
     * when there are more than 3 followed posts
     * @throws InterruptedException
     */
    @Test
    public void filter3RecentFollowedPostsDisplayOnly3Post() throws InterruptedException {
        // add more events to database
        addMoreEvent("moreTest1", "Test extra yesterday 1", -1);
        addMoreEvent("moreTest2", "Test extra yesterday 2", -2);
        logIn();

        //Ensure that test is on followed tab
        onView(withId(R.id.tab_followed)).perform(click());

        //Ensure correct number of added events
        onView(withId(R.id.recycler_view))
                .check((view, noViewFoundException) -> {
                    RecyclerView recyclerView = (RecyclerView) view;
                    int itemCount = recyclerView.getAdapter().getItemCount();
                    assertEquals("RecyclerView should have exactly 4 items", 4, itemCount);
                });


        //click on menu button to select filter
        onView(withId(R.id.menu_button)).perform(click());
        onView(withId(R.id.three_recent_option)).perform(click());

        //Check if only 3 posts are displayed
        onView(withId(R.id.recycler_view))
                .check((view, noViewFoundException) -> {
                    RecyclerView recyclerView = (RecyclerView) view;
                    int itemCount = recyclerView.getAdapter().getItemCount();
                    assertEquals("RecyclerView should have exactly 3 items", 3, itemCount);
                });

    }

    /**
     * Test filtering show 3 most recent posts should display all followed posts
     * when there are less than 3 followed posts
     * @throws InterruptedException
     */
    @Test
    public void filter3RecentFollowedPostsDisplayAllPostsWhenFollowedPostsLessThan3Posts() throws InterruptedException {
        logIn();

        //Ensure that test is on followed tab
        onView(withId(R.id.tab_followed)).perform(click());
        Thread.sleep(1000);

        //Make sure added events are displayed
        onView(withText("Test during recent week")).check(ViewAssertions.matches(isDisplayed()));
        onView(withText("Test last year")).check(ViewAssertions.matches(isDisplayed()));

        //click on menu button to select filter
        onView(withId(R.id.menu_button)).perform(click());
        onView(withId(R.id.three_recent_option)).perform(click());

        //Check if the same events are displayed
        onView(withText("Test during recent week")).check(ViewAssertions.matches(isDisplayed()));
        onView(withText("Test last year")).check(ViewAssertions.matches(isDisplayed()));
    }

    /**
     * Test filtering show 3 most recent posts should only display
     * the three most recent posts
     * @throws InterruptedException
     */
    @Test
    public void filter3RecentFollowedPostsDisplayRecentPosts() throws InterruptedException {
        // add more events to the database
        addMoreEvent("moreTest1", "Test extra yesterday 1", -1);
        addMoreEvent("moreTest2", "Test extra yesterday 2", -2);
        logIn();


        //Ensure that test is on followed tab
        onView(withId(R.id.tab_followed)).perform(click());
        Thread.sleep(1000);

        //Make sure added events are displayed
        onView(withText("Test during recent week")).check(ViewAssertions.matches(isDisplayed()));
        onView(withText("Test extra yesterday 1")).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.recycler_view)).perform(swipeUp());
        onView(withText("Test extra yesterday 2")).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.recycler_view)).perform(swipeUp());
        onView(withText("Test last year")).check(ViewAssertions.matches(isDisplayed()));

        //click on menu button to select filter
        onView(withId(R.id.menu_button)).perform(click());
        onView(withId(R.id.three_recent_option)).perform(click());

        //Check if the three recent posts are displayed
        onView(withText("Test during recent week")).check(ViewAssertions.matches(isDisplayed()));
        onView(withText("Test extra yesterday 1")).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.recycler_view)).perform(swipeUp());
        onView(withText("Test extra yesterday 2")).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.recycler_view)).perform(swipeUp());
    }

    /**
     * Test filtering show 3 most recent posts should not show the non recent posts
     * that were posted before the recent 3
     */
    @Test
    public void filter3RecentFollowedPostsShouldNotDisplayNonRecentPosts() throws InterruptedException {
        // add more events to database
        addMoreEvent("moreTest1", "Test extra yesterday 1", -1);
        addMoreEvent("moreTest2", "Test extra yesterday 2", -2);
        logIn();

        //Ensure that test is on followed tab
        onView(withId(R.id.tab_followed)).perform(click());
        Thread.sleep(1000);

        //Make sure added events are displayed
        onView(withText("Test during recent week")).check(ViewAssertions.matches(isDisplayed()));
        onView(withText("Test extra yesterday 1")).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.recycler_view)).perform(swipeUp());
        onView(withText("Test extra yesterday 2")).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.recycler_view)).perform(swipeUp());
        onView(withText("Test last year")).check(ViewAssertions.matches(isDisplayed()));

        //click on menu button to select filter
        onView(withId(R.id.menu_button)).perform(click());
        onView(withId(R.id.three_recent_option)).perform(click());
        onView(withId(R.id.recycler_view)).perform(swipeUp());

        // make sure non recent event outside of the 3 recent ones are not displayed
        onView(withText("Test last year")).check(ViewAssertions.doesNotExist());

    }

    /**
     * Add base events and users that are needed in the testing to the database
     * @throws InterruptedException wait for user and posts database to load in firestore and onto the tab
     */
    @Before // run before every test
    public void setUp() throws InterruptedException {
        seedUserDB();
        seedPostDB();
    }


    /**
     * Log in to enter homepage using testUser who is following followedUser
     * @throws InterruptedException wait for all user and post data to load
     */
    public void logIn() throws InterruptedException {
        onView(withId(R.id.login_button_login_page)).perform(click());


        onView(withId(R.id.login_username)).perform(typeText("testUser")).perform(closeSoftKeyboard());
        onView(withId(R.id.login_password)).perform(typeText("TestPass")).perform(closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        Thread.sleep(5000);
    }

    /**
     * Set up user and its following in the database
     * @throws InterruptedException attempt adding user to database
     */
    private void seedUserDB() throws InterruptedException {
        String username = "testUser", name = "Test User", rawPassword = "TestPass";
        String username2 = "followedUser", name2 = "Test User Logged In", rawPassword2 = "TestPasss";
        seedUser(username, name, rawPassword);
        seedUser(username2, name2, rawPassword2);


        followUser(username, username2, success -> {
            Log.d("FollowDebug", "followedUser successfully follow testUser");
        });


    }

    /**
     * Add a user to the database
     * @param username          username of the account
     * @param name              name of the user
     * @param rawPassword       raw password to be hashed
     * @throws InterruptedException wait for latch release when attemp to add user to database
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

    /**
     * Set followingUser to follow followedUser
     * @param followingUser     user that is following the other user
     * @param followedUser      user that is being followed
     * @param callback          follow whether action is successful or not
     */
    public void followUser(String followingUser, String followedUser, Userbase.FollowActionCallback callback){
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
     * Add posts to database with current date and last year date
     * @throws InterruptedException wait for event to be added to database
     */
    public void seedPostDB() throws InterruptedException {
        Event event = new Event("TestBeforeRecWk", "Test last year", "12 MAR 2024, 12:04", "#CC0099", "", 0, true, false, "Surprise", "", "", "followedUser", null, null, true);
        Event event2 = new Event("TestDuringRecWk", "Test during recent week", null, "#FF6347", "", 0, true, false, "Anger", "", "", "followedUser", null, null, true);


        // current date to ensure test will work regardless of the time
        String eventDate = new SimpleDateFormat("dd MMM yyyy, HH:mm",
                Locale.getDefault()).format(new Date());
        event2.setDate(eventDate);


        seedPost(event);
        seedPost(event2);
    }

    /**
     * Add an event to database
     * @param event     event of an Event class containing id, title, date, etc.
     * @throws InterruptedException     wait for latch to release when attempt to add event to firestore
     */
    public void seedPost(Event event) throws InterruptedException {
        Database db = new Database();
        CollectionReference postsRef = db.getMyPostsRef();
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism
        postsRef.document(event.getID()).set(event)
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
     * Add an event to database with specified number of day(s) from current date
     * @param eventID           id of an event
     * @param eventTitle        title of a mood event
     * @param subtractDay       day to subtract from current date (-1 for yesterday etc.)
     * @throws InterruptedException wait for result of adding event to firestore
     */
    public void addMoreEvent(String eventID, String eventTitle, int subtractDay) throws InterruptedException {
        Event event = new Event(eventID, eventTitle, null, "#FF6347", "", 0, true, false, "Anger", "", "", "followedUser", null, null, true);
        // current date to ensure test will work regardless of the time
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, subtractDay); // Subtract one day


        String yesterdayDate = new SimpleDateFormat("dd MMM yyyy, HH:mm",
                Locale.getDefault()).format(calendar.getTime());


        event.setDate(yesterdayDate);
        seedPost(event);
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
