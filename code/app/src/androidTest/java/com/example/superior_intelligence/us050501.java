package com.example.superior_intelligence;
/**
 * Test filtering by emotional state for followed posts
 */

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

@RunWith(JUnit4.class)
public class us050501 {

    // Start at HomeActivity
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Test filtering followed post should show followed posts-
     * with one selected emotional state
     * @throws InterruptedException wait for event to load when filter is applied
     */
    @Test
    public void filterFollowedPostShouldShowPostsMatchingOneSelectedEmotionalState() throws InterruptedException {
        checkEventsPresent();

        openEmotionalStateDialog();
        selectEmotionalState("Anger");
        onView(withText("FILTER")).perform(click());
        //Wait for event to load
        Thread.sleep(1000);

        //check matching emotional state should be displayed
        onView(withText("Test angry")).check(ViewAssertions.matches(isDisplayed()));

        //check non-matching emotional state should not be displayed
        onView(withText("Test surprise")).check(doesNotExist());
    }

    /**
     * Test filtering followed post should show followed posts-
     * with two selected emotional state
     * @throws InterruptedException wait for events to load after filter is applied
     */
    @Test
    public void filterFollowedPostShouldShowPostsMatchingTwoSelectedEmotionalState() throws InterruptedException {
        checkEventsPresent();

        openEmotionalStateDialog();
        selectEmotionalState("Anger");
        selectEmotionalState("Surprise");
        onView(withText("FILTER")).perform(click());
        //Wait for event to load
        Thread.sleep(1000);

        //check matching emotional state should be displayed
        onView(withText("Test angry")).check(ViewAssertions.matches(isDisplayed()));
        onView(withText("Test surprise")).check(ViewAssertions.matches(isDisplayed()));
    }
    /**
     * Test filtering followed post should remain the same when no emotional state is selected
     * but the filter button is clicked
     * @throws InterruptedException wait for dialog to load
     */
    @Test
    public void filterFollowedPostShouldShowNoDifferenceWhenNoEmotionalStateIsSelected() throws InterruptedException {
        checkEventsPresent();

        openEmotionalStateDialog();
        selectEmotionalState("Anger");
        selectEmotionalState("Surprise");
        onView(withText("FILTER")).perform(click());

        //the same events should be displayed
        checkEventsPresent();
    }
    /**
     * Select emotional state on the check box
     * @param emotionalState    Emotional state of the mood
     */
    public void selectEmotionalState(String emotionalState){
        onView(withText(emotionalState)).perform(click());
    }

    /**
     * Click on filter menu and select filter by emotional state option to open the dialog
     * @throws InterruptedException wait for dialog to load
     */
    public void openEmotionalStateDialog() throws InterruptedException {
        // enter followed tab
        onView(withId(R.id.tab_followed)).perform(click());

        // click filter menu and open filter by emotional state dialog
        onView(withId(R.id.menu_button)).perform(click());
        onView(withId(R.id.emotional_state_option)).perform(click());
        Thread.sleep(1000);
    }

    /**
     * Add base events and users that are needed in the testing to the database
     * @throws InterruptedException wait for firebase latch and user and data users to load
     */
    @Before // run before every test
    public void setUp() throws InterruptedException {
        seedUserDB();
        seedPostDB();
        logIn();
    }

    /**
     * Check that seeded events are present
     */
    public void checkEventsPresent(){
        onView(withText("Test angry")).check(ViewAssertions.matches(isDisplayed()));
        onView(withText("Test surprise")).check(ViewAssertions.matches(isDisplayed()));
    }

    /**
     * Log in to enter homepage using testUser who is following followedUser
     * Switch to followed tab
     * @throws InterruptedException     wait for all user and post data to load
     */
    public void logIn() throws InterruptedException {
        onView(withId(R.id.login_button_login_page)).perform(click());

        onView(withId(R.id.login_username)).perform(typeText("testUser")).perform(closeSoftKeyboard());
        onView(withId(R.id.login_password)).perform(typeText("TestPass")).perform(closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        Thread.sleep(5000);

        onView(withId(R.id.tab_followed)).perform(click());
        Thread.sleep(3000);
    }

    /**
     * Set up user and its following in the database
     * @throws  InterruptedException wait for latch to unlock when following other user in database
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
     * @param username      unique username of the user
     * @param name          name of the user
     * @param rawPassword   raw password of the account to be hashed
     * @throws InterruptedException     wait for latch to be released when user is added to firebase
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
     * Set in the database for following user to follow followedUser
     * @param followingUser     user who is requesting to follow
     * @param followedUser      user who is being followed
     * @param callback          for success/failure listener
     * @throws InterruptedException wait for following process to complete in database
     */
    public void followUser(String followingUser, String followedUser, Userbase.FollowActionCallback callback) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism
        db.collection("users").document(followingUser)
                .update("following", FieldValue.arrayUnion(followedUser))
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(followedUser)
                            .update("followers", FieldValue.arrayUnion(followingUser))
                            .addOnSuccessListener(aVoid2 -> callback.onFollowAction(true))
                            .addOnFailureListener(e -> callback.onFollowAction(false));
                })
                .addOnFailureListener(e -> callback.onFollowAction(false));
        latch.await();
    }

    /**
     * Add posts to database with current date and last year date
     * @throws InterruptedException wait for each post to be added successfully to database
     */
    public void seedPostDB() throws InterruptedException {
        Event event = new Event("TestSurprise", "Test surprise", "12 MAR 2024, 12:04", "#CC0099", "", 0, true, false, "Surprise", "", "", "followedUser", null, null, true);
        Event event2 = new Event("TestAngry", "Test angry", null, "#FF6347", "", 0, true, false, "Anger", "", "", "followedUser", null, null, true);


        // current date to ensure test will work regardless of the time
        String eventDate = new SimpleDateFormat("dd MMM yyyy, HH:mm",
                Locale.getDefault()).format(new Date());
        event2.setDate(eventDate);


        seedPost(event);
        seedPost(event2);
    }

    /**
     * Add an event to database
     * @param event     event to be added to the database
     * @throws InterruptedException wait for latch to release when adding event to firebase
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
